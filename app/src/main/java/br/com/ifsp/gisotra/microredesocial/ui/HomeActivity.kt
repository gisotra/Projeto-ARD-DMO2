package br.com.ifsp.gisotra.microredesocial.ui

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import br.com.ifsp.gisotra.microredesocial.adapter.PostAdapter
import br.com.ifsp.gisotra.microredesocial.data.model.Post
import br.com.ifsp.gisotra.microredesocial.databinding.ActivityHomeBinding
import br.com.ifsp.gisotra.microredesocial.service.AddPostActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var postAdapter: PostAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val listaDePosts = mutableListOf<Post>()

    // --- VARIÁVEIS DE PAGINAÇÃO POR CURSOR ---
    private val cursoresAnteriores = mutableListOf<DocumentSnapshot>()
    private var ultimoDocumentoDaPagina: DocumentSnapshot? = null
    private var paginaAtual = 1
    private val LIMIT = 5L // Limite de 5 posts por vez
    private var isBuscandoPorCidade = false
    private var cidadeBuscada = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (auth.currentUser == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        configurarRecyclerView()
        configurarBotoes()
        carregarPagina(null) // null significa: carregar a primeira página do zero
    }

    override fun onResume() {
        super.onResume()
        carregarPerfilUsuario()
    }

    private fun carregarPerfilUsuario() {
        val email = auth.currentUser?.email
        if (email != null) {
            db.collection("users").document(email).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val nome = document.getString("nome") ?: "Usuário"
                        binding.txtUserNameHome.text = "Olá, $nome!"

                        val fotoBase64 = document.getString("foto")
                        if (!fotoBase64.isNullOrEmpty()) {
                            try {
                                val imageBytes = Base64.decode(fotoBase64, Base64.DEFAULT)
                                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                binding.imgUserProfileHome.setImageBitmap(decodedImage)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
        }
    }

    private fun configurarRecyclerView() {
        postAdapter = PostAdapter(listaDePosts)
        binding.rvPosts.layoutManager = LinearLayoutManager(this)
        binding.rvPosts.adapter = postAdapter
        // Scroll infinito foi removido daqui!
    }

    private fun configurarBotoes() {
        binding.btnNovoPost.setOnClickListener {
            startActivity(Intent(this, AddPostActivity::class.java))
        }

        binding.btnPerfil.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.btnBuscar.setOnClickListener {
            val cidade = binding.edtBuscaCidade.text.toString().trim()
            if (cidade.isNotEmpty()) {
                isBuscandoPorCidade = true
                cidadeBuscada = normalizarTexto(cidade) // Limpa o texto antes de buscar!
                carregarPagina(null)
            } else {
                isBuscandoPorCidade = false
                cidadeBuscada = ""
                carregarPagina(null)
            }
        }

        // Botoes de Navegação
        binding.btnAnterior.setOnClickListener {
            carregarPagina(false) // Volta uma página
        }

        binding.btnProximo.setOnClickListener {
            carregarPagina(true) // Avança uma página
        }
    }

    // A MÁGICA DA PAGINAÇÃO POR CURSOR
    private fun carregarPagina(avancar: Boolean?) {
        // avancar = true (Próximo) | false (Anterior) | null (Resetar/Primeira)

        var query = db.collection("post").orderBy("data", Query.Direction.DESCENDING).limit(LIMIT)

        // Usa o "cidadeFiltro" para a busca funcionar ignorando maiúsculas e acentos
        if (isBuscandoPorCidade && cidadeBuscada.isNotEmpty()) {
            query = db.collection("post")
                .whereEqualTo("cidadeFiltro", cidadeBuscada)
                .orderBy("data", Query.Direction.DESCENDING)
                .limit(LIMIT)
        }

        if (avancar == true) {
            // Se vai avançar, guarda o último item desta página para poder voltar depois
            if (ultimoDocumentoDaPagina != null) {
                cursoresAnteriores.add(ultimoDocumentoDaPagina!!)
            }
            paginaAtual++
        } else if (avancar == false) {
            // Se vai voltar, remove o último cursor da memória
            if (cursoresAnteriores.isNotEmpty()) {
                cursoresAnteriores.removeAt(cursoresAnteriores.size - 1)
            }
            paginaAtual--
        } else {
            // Se for null, limpa todo o histórico e reseta pra página 1
            cursoresAnteriores.clear()
            paginaAtual = 1
            ultimoDocumentoDaPagina = null
        }

        // Se a gente tem um cursor na memória, diz pro Firebase: "Começa a partir desse aqui"
        if (cursoresAnteriores.isNotEmpty()) {
            query = query.startAfter(cursoresAnteriores.last())
        }

        // Bloqueia os botões enquanto carrega para o usuário não clicar mil vezes
        binding.btnAnterior.isEnabled = false
        binding.btnProximo.isEnabled = false

        query.get().addOnSuccessListener { documentos ->
            listaDePosts.clear()

            if (documentos.size() > 0) {
                // Atualiza a nossa referência de "último documento"
                ultimoDocumentoDaPagina = documentos.documents[documentos.size() - 1]

                for (doc in documentos) {
                    val post = doc.toObject(Post::class.java)
                    listaDePosts.add(post)
                }
            }

            postAdapter.notifyDataSetChanged()

            // Atualiza os Textos e os Botões
            binding.txtPaginaAtual.text = "Página $paginaAtual"

            // Só pode voltar se a página for maior que 1
            binding.btnAnterior.isEnabled = paginaAtual > 1

            // Só pode avançar se o banco mandou EXATAMENTE 5. Se mandou 4 ou menos, é porque acabaram os posts.
            binding.btnProximo.isEnabled = documentos.size() == LIMIT.toInt()

            if (listaDePosts.isEmpty() && paginaAtual == 1) {
                Toast.makeText(this, "Nenhum post encontrado", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Erro ao carregar feed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun normalizarTexto(texto: String): String {
        val semAcento = java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        return semAcento.lowercase().trim()
    }
}