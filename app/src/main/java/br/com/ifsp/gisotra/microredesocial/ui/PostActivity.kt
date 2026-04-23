package br.com.ifsp.gisotra.microredesocial.ui

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.com.ifsp.gisotra.microredesocial.R
import br.com.ifsp.gisotra.microredesocial.databinding.ActivityPostBinding

class PostActivity : AppCompatActivity() {
    // Configurando o ViewBinding
    private lateinit var binding: ActivityPostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inflando o layout com ViewBinding
        binding = ActivityPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        carregarDadosDoPost()
    }

    private fun carregarDadosDoPost() {
        // Recebendo os dados passados pelo Intent lá da tela do Feed
        val descricao = intent.getStringExtra("descricao") ?: "Sem descrição"
        val autor = intent.getStringExtra("autor") ?: "Autor desconhecido"
        val cidade = intent.getStringExtra("cidade") ?: "Local desconhecido"
        val imagemBase64 = intent.getStringExtra("imagem")

        // Preenchendo os textos na tela
        binding.txtDescricao.text = descricao
        binding.txtNomeAutor.text = autor
        binding.txtCidade.text = "📍 $cidade"

        // Decodificando a imagem Base64 de volta para Bitmap
        if (!imagemBase64.isNullOrEmpty()) {
            try {
                val imageBytes = Base64.decode(imagemBase64, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                binding.imgPost.setImageBitmap(decodedImage)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
