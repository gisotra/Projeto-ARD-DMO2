package br.com.ifsp.gisotra.microredesocial.service


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import br.com.ifsp.gisotra.microredesocial.databinding.ActivityAddPostBinding
import br.com.ifsp.gisotra.microredesocial.ui.HomeActivity
import br.com.ifsp.gisotra.microredesocial.tool.Base64Converter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class AddPostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddPostBinding

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

        binding.btnChangePhoto.setOnClickListener {
            galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnSave.setOnClickListener {
            val firebaseAuth = FirebaseAuth.getInstance()
            val db = FirebaseFirestore.getInstance()

            val user = firebaseAuth.currentUser
            if (user != null) {

                val descricao = binding.edtDescricao.text.toString()
                val fotoString = Base64Converter.drawableToString(binding.imgPost.drawable)

                if (descricao.isBlank()) {
                    Toast.makeText(this, "Digite uma descrição", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val dados = hashMapOf(
                    "descricao" to descricao,
                    "imageString" to fotoString,
                    "data" to Timestamp.now()
                )

                db.collection("posts")
                    .add(dados)
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
    }
}