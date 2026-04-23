package br.com.ifsp.gisotra.microredesocial.ui

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import br.com.ifsp.gisotra.microredesocial.databinding.ActivityProfileBinding
import br.com.ifsp.gisotra.microredesocial.tool.Base64Converter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Lançador da galeria (Igual usamos no AddPostActivity)
    private val galeria = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            binding.imgProfile.setImageURI(uri)
        } else {
            Toast.makeText(this, "Nenhuma foto selecionada", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        carregarDadosAtuais()
        configurarBotoes()
    }

    private fun carregarDadosAtuais() {
        val user = auth.currentUser
        if (user != null) {
            // Preenche o nome atual
            binding.edtProfileName.setText(user.displayName)

            // Busca a foto de perfil lá do Firestore (coleção 'users', documento = ID do usuário)
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val fotoBase64 = document.getString("fotoPerfil")
                        if (!fotoBase64.isNullOrEmpty()) {
                            try {
                                val imageBytes = Base64.decode(fotoBase64, Base64.DEFAULT)
                                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                binding.imgProfile.setImageBitmap(decodedImage)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
        }
    }

    private fun configurarBotoes() {
        binding.btnChangeProfilePhoto.setOnClickListener {
            galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnSaveProfile.setOnClickListener {
            salvarAlteracoes()
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut() // Desloga do Firebase
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Limpa a pilha de telas
            startActivity(intent)
            finish()
        }
    }

    private fun salvarAlteracoes() {
        val user = auth.currentUser ?: return
        val novoNome = binding.edtProfileName.text.toString().trim()
        val novaSenha = binding.edtProfilePassword.text.toString().trim()

        if (novoNome.isEmpty()) {
            Toast.makeText(this, "O nome não pode ficar vazio!", Toast.LENGTH_SHORT).show()
            return
        }

        // 1. Atualiza o Nome
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(novoNome)
            .build()

        user.updateProfile(profileUpdates).addOnFailureListener {
            Toast.makeText(this, "Erro ao atualizar nome", Toast.LENGTH_SHORT).show()
        }

        // 2. Atualiza a Senha (apenas se ele digitou algo)
        if (novaSenha.isNotEmpty()) {
            if (novaSenha.length >= 6) {
                user.updatePassword(novaSenha).addOnFailureListener {
                    Toast.makeText(this, "Erro ao atualizar senha. Pode ser necessário deslogar e logar de novo.", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show()
            }
        }

        // 3. Atualiza a Foto de Perfil no Firestore
        if (binding.imgProfile.drawable != null) {
            val fotoString = Base64Converter.drawableToString(binding.imgProfile.drawable)

            val userData = hashMapOf(
                "fotoPerfil" to fotoString
            )

            // Salva na coleção "users" usando o ID do próprio usuário (uid)
            db.collection("users").document(user.uid)
                .set(userData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                    finish() // Volta pra Home
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Erro ao salvar foto", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}