package br.com.ifsp.gisotra.microredesocial.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import br.com.ifsp.gisotra.microredesocial.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

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
            binding.edtProfileName.setText(user.displayName)
            val emailUsuario = user.email

            if (emailUsuario != null) {
                // BUSCA PELO E-MAIL (Igual fizemos no cadastro!)
                db.collection("users").document(emailUsuario).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            // O NOME DO CAMPO É "foto" (igual no cadastro)
                            val fotoBase64 = document.getString("foto")
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
    }

    private fun configurarBotoes() {
        binding.btnChangeProfilePhoto.setOnClickListener {
            galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnSaveProfile.setOnClickListener {
            salvarAlteracoes()
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        binding.btnVoltar.setOnClickListener {
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

        // 1. Atualiza o Nome no Firebase Auth
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(novoNome)
            .build()

        user.updateProfile(profileUpdates).addOnFailureListener {
            Toast.makeText(this, "Erro ao atualizar nome", Toast.LENGTH_SHORT).show()
        }

        // 2. Atualiza o Nome no Firestore também
        val emailUsuario = user.email
        if (emailUsuario != null) {
            db.collection("users").document(emailUsuario).update("nome", novoNome)
        }

        // 3. Atualiza a Senha
        if (novaSenha.isNotEmpty()) {
            if (novaSenha.length >= 6) {
                user.updatePassword(novaSenha).addOnFailureListener {
                    Toast.makeText(this, "Erro ao atualizar senha. Logue novamente.", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show()
            }
        }

// 4. COMPRIME E SALVA A FOTO NO FIRESTORE
        if (binding.imgProfile.drawable != null && emailUsuario != null) {
            try {
                val bitmap = (binding.imgProfile.drawable as BitmapDrawable).bitmap
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 400, 400, true)
                val baos = ByteArrayOutputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
                val fotoString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)

                // MUDANÇA AQUI: Usando o SetOptions.merge() para não dar erro se o documento não existir
                val dadosAtualizados = hashMapOf("foto" to fotoString)

                db.collection("users").document(emailUsuario)
                    .set(dadosAtualizados, com.google.firebase.firestore.SetOptions.merge()) // <-- O PULO DO GATO
                    .addOnSuccessListener {
                        Toast.makeText(this, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Erro ao salvar foto no banco", Toast.LENGTH_SHORT).show()
                    }
            } catch (e: Exception) {
                Toast.makeText(this, "Erro ao processar imagem", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}