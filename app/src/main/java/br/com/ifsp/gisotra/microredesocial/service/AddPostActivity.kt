package br.com.ifsp.gisotra.microredesocial.service

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Address
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import br.com.ifsp.gisotra.microredesocial.data.model.Post
import br.com.ifsp.gisotra.microredesocial.databinding.ActivityAddPostBinding
import br.com.ifsp.gisotra.microredesocial.tool.LocalizacaoHelper
import br.com.ifsp.gisotra.microredesocial.ui.HomeActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class AddPostActivity : AppCompatActivity(), LocalizacaoHelper.Callback {
    private lateinit var binding: ActivityAddPostBinding
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    private val galeria = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            binding.imgPost.setImageURI(uri)
        } else {
            Toast.makeText(this, "Nenhuma foto selecionada", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnChangePhoto.setOnClickListener {
            galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnAtualizar.setOnClickListener {
            solicitarLocalizacao()
        }

        binding.btnSave.setOnClickListener {
            salvarPost()
        }

        binding.btnVoltar.setOnClickListener {
            finish()
        }
    }

    private fun salvarPost() {
        val firebaseAuth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val user = firebaseAuth.currentUser

        if (user != null) {
            val emailUsuario = user.email ?: return // Pegamos o e-mail para buscar a foto depois

            val descricao = binding.edtDescricao.text.toString().trim()
            val cidade = binding.edtCidade.text.toString().trim()

            if (binding.imgPost.drawable == null) {
                Toast.makeText(this, "Selecione uma foto!", Toast.LENGTH_SHORT).show()
                return
            }

            if (descricao.isEmpty() || cidade.isEmpty()) {
                Toast.makeText(this, "Preencha a descrição e a cidade!", Toast.LENGTH_SHORT).show()
                return
            }

            // --- MAGIA DA COMPRESSÃO (Evita o Erro de 1MB do Firestore) ---
            val bitmap = (binding.imgPost.drawable as BitmapDrawable).bitmap
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 500, 500, true)
            val baos = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
            val fotoString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
            // ---------------------------------------------------------------

            // 1. Busca a foto do dono da postagem lá na coleção "users"
            db.collection("users").document(emailUsuario).get().addOnSuccessListener { document ->
                // Pega a foto de perfil em Base64. Se não tiver, fica uma String vazia ""
                val fotoDoAutorBase64 = document.getString("foto") ?: ""

                // 2. Monta o objeto Post. AGORA COM A FOTO DO AUTOR!
                val novoPost = Post(
                    descricao = descricao,
                    imagem = fotoString,
                    data = Timestamp.now(),
                    nomeAutor = user.displayName ?: "Usuário",
                    cidade = cidade,
                    cidadeFiltro = normalizarTexto(cidade),
                    fotoAutor = fotoDoAutorBase64 // <-- INSERIMOS A FOTO AQUI
                )

                // 3. Salva na coleção "post"
                db.collection("post")
                    .add(novoPost)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Post salvo com sucesso!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Erro ao salvar o post: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }.addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar perfil do usuário para o post", Toast.LENGTH_SHORT).show()
            }

        } else {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
        }
    }

    // --- MÉTODOS DE GEOLOCALIZAÇÃO ---
    private fun solicitarLocalizacao() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            Toast.makeText(this, "Buscando...", Toast.LENGTH_SHORT).show()
            val localizacaoHelper = LocalizacaoHelper(applicationContext)
            localizacaoHelper.obterLocalizacaoAtual(this)
        }
    }

    override fun onLocalizacaoRecebida(endereco: Address, latitude: Double, longitude: Double) {
        runOnUiThread {
            // Tenta pegar 'locality' primeiro. Se for nulo, tenta 'subAdminArea'. Se der ruim, põe Desconhecido.
            val cidadeAtual = endereco.locality ?: endereco.subAdminArea ?: endereco.adminArea ?: "Desconhecido"

            binding.edtCidade.setText(cidadeAtual)
            Toast.makeText(this, "Localização atualizada!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onErro(mensagem: String) {
        runOnUiThread { Toast.makeText(this, "Erro GPS: $mensagem", Toast.LENGTH_SHORT).show() }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            solicitarLocalizacao()
        } else {
            Toast.makeText(this, "Permissão de localização negada", Toast.LENGTH_SHORT).show()
        }
    }

    // lidar com acentos, caixa alta, etc
    private fun normalizarTexto(texto: String): String {
        val semAcento = java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        return semAcento.lowercase().trim()
    }
}