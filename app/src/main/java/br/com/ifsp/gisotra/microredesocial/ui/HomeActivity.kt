package br.com.ifsp.gisotra.microredesocial.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    // Variáveis para a Paginação (RF3-1)
    private val listaDePosts = mutableListOf<Post>()
    private var ultimoDocumento: DocumentSnapshot? = null
    private var carregando = false
    private var isBuscandoPorCidade = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Se por algum motivo o usuário não estiver logado, chuta ele pro Login
        if (auth.currentUser == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        configurarRecyclerView()
        configurarBotoes()
        carregarPosts(false) // Carrega os primeiros 5 posts
    }

    private fun configurarRecyclerView() {
        postAdapter = PostAdapter(listaDePosts)
        binding.rvPosts.layoutManager = LinearLayoutManager(this)
        binding.rvPosts.adapter = postAdapter

        // Lógica de Paginação: Quando chegar no final da lista, puxa mais 5
        binding.rvPosts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && !carregando && !isBuscandoPorCidade) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    if (layoutManager.findLastCompletelyVisibleItemPosition() == listaDePosts.size - 1) {
                        carregarPosts(false)
                    }
                }
            }
        })
    }

    private fun configurarBotoes() {
        // Navegação
        binding.btnNovoPost.setOnClickListener {
            startActivity(Intent(this, AddPostActivity::class.java))
        }

        binding.btnPerfil.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Busca por Cidade (RF3-2)
        binding.btnBuscar.setOnClickListener {
            val cidade = binding.edtBuscaCidade.text.toString().trim()
            if (cidade.isNotEmpty()) {
                isBuscandoPorCidade = true
                listaDePosts.clear()
                postAdapter.notifyDataSetChanged()
                buscarPostsPorCidade(cidade)
            } else {
                // Se esvaziar a busca, recarrega o feed normal
                isBuscandoPorCidade = false
                listaDePosts.clear()
                ultimoDocumento = null
                carregarPosts(true)
            }
        }
    }

    private fun carregarPosts(limparLista: Boolean) {
        if (carregando) return
        carregando = true

        if (limparLista) {
            listaDePosts.clear()
            ultimoDocumento = null
        }

        // Puxa do Firestore ordenado pela data, de 5 em 5 (RF3-1)
        var query = db.collection("post")
            .orderBy("data", Query.Direction.DESCENDING)
            .limit(5)

        if (ultimoDocumento != null) {
            query = query.startAfter(ultimoDocumento!!)
        }

        query.get().addOnSuccessListener { documentos ->
            if (documentos.size() > 0) {
                ultimoDocumento = documentos.documents[documentos.size() - 1]

                val novosPosts = mutableListOf<Post>()
                for (doc in documentos) {
                    val post = doc.toObject(Post::class.java)
                    novosPosts.add(post)
                }
                postAdapter.adicionarPosts(novosPosts)
            }
            carregando = false
        }.addOnFailureListener {
            Toast.makeText(this, "Erro ao carregar feed", Toast.LENGTH_SHORT).show()
            carregando = false
        }
    }

    private fun buscarPostsPorCidade(cidade: String) {
        // Busca exata pelo nome da cidade no Firebase
        db.collection("post")
            .whereEqualTo("cidade", cidade)
            .get()
            .addOnSuccessListener { documentos ->
                val postsDaCidade = mutableListOf<Post>()
                for (doc in documentos) {
                    val post = doc.toObject(Post::class.java)
                    postsDaCidade.add(post)
                }
                if (postsDaCidade.isEmpty()) {
                    Toast.makeText(this, "Nenhum post nessa cidade", Toast.LENGTH_SHORT).show()
                } else {
                    postAdapter.adicionarPosts(postsDaCidade)
                }
            }
    }
}