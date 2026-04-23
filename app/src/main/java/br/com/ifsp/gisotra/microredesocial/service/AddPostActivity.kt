package br.com.ifsp.gisotra.microredesocial.service


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import br.com.ifsp.gisotra.microredesocial.data.model.Post
import br.com.ifsp.gisotra.microredesocial.databinding.ActivityAddPostBinding
import br.com.ifsp.gisotra.microredesocial.ui.HomeActivity
import br.com.ifsp.gisotra.microredesocial.tool.Base64Converter
import br.com.ifsp.gisotra.microredesocial.tool.LocalizacaoHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class AddPostActivity : AppCompatActivity(), LocalizacaoHelper.Callback {
    private lateinit var binding: ActivityAddPostBinding
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    private var cidadeAtual: String = ""
    private val galeria = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            binding.imgPost.setImageURI(uri)
        } else {
            Toast.makeText(this, "Nenhuma foto selecionada", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Botão para pegar a foto
        binding.btnChangePhoto.setOnClickListener {
            galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        // Botão para atualizar localização (Lembra de colocar esse botão no activity_add_post.xml!)
        binding.btnAtualizar.setOnClickListener {
            solicitarLocalizacao()
        }

        // Botão de salvar
        binding.btnSave.setOnClickListener {
            salvarPost()
        }
    }

    private fun salvarPost() {
        val firebaseAuth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val user = firebaseAuth.currentUser

        if (user != null) {
            val descricao = binding.edtDescricao.text.toString()

            // Tratamento caso o usuário não tenha selecionado uma imagem
            if (binding.imgPost.drawable == null) {
                Toast.makeText(this, "Selecione uma imagem!", Toast.LENGTH_SHORT).show()
                return
            }

            val fotoString = Base64Converter.drawableToString(binding.imgPost.drawable)

            if (descricao.isBlank()) {
                Toast.makeText(this, "Digite uma descrição", Toast.LENGTH_SHORT).show()
                return
            }

            // Usando a classe Post para enviar tudo pro Firestore de forma organizada!
            val novoPost = Post(
                descricao = descricao,
                imagem = fotoString,
                data = Timestamp.now(),
                nomeAutor = user.displayName ?: "Usuário", // Pega o nome definido no RF1
                cidade = cidadeAtual // Salva a cidade que o GPS achou
            )

            // Salvando na coleção "post" (Igual a print que você mandou do console)
            db.collection("post")
                .add(novoPost)
                .addOnSuccessListener {
                    Toast.makeText(this, "Post salvo com sucesso!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Erro ao salvar o post", Toast.LENGTH_SHORT).show()
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
            val localizacaoHelper = LocalizacaoHelper(applicationContext)
            localizacaoHelper.obterLocalizacaoAtual(this)
        }
    }

    override fun onLocalizacaoRecebida(endereco: Address, latitude: Double, longitude: Double) {
        runOnUiThread {
            // Pega o nome da cidade. Se subAdminArea for nula, tenta adminArea
            cidadeAtual = endereco.subAdminArea ?: endereco.adminArea ?: "Cidade Desconhecida"

            // Opcional: Atualizar um TextView na tela para o usuário ver a cidade
            // binding.txtCidade.text = "Local: $cidadeAtual"
            Toast.makeText(this, "Localização atualizada: $cidadeAtual", Toast.LENGTH_SHORT).show()
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
}