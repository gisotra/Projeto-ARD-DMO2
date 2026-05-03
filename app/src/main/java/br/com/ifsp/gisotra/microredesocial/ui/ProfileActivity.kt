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
import com.google.firebase.firestore.SetOptions
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
                // Busca os dados complementares no Firestore
                db.collection("users").document(emailUsuario).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
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
        val emailUsuario = user.email ?: return

        if (novoNome.isEmpty()) {
            Toast.makeText(this, "O nome não pode ficar vazio!", Toast.LENGTH_SHORT).show()
            return
        }

        // 1. Prepara o pacote de dados para o Firestore
        val dadosAtualizados = hashMapOf<String, Any>(
            "nome" to novoNome
        )

        // 2. Comprime e anexa a foto no pacote (se houver foto)
        if (binding.imgProfile.drawable != null) {
            try {
                val bitmap = (binding.imgProfile.drawable as BitmapDrawable).bitmap
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 400, 400, true)
                val baos = ByteArrayOutputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
                val fotoString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
                dadosAtualizados["foto"] = fotoString
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 3. Inicia a atualização oficial do perfil no Auth
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(novoNome)
            .build()

        user.updateProfile(profileUpdates).addOnCompleteListener { task ->
            if (task.isSuccessful) {

                // 4. Se o Auth atualizou, envia o pacote com Nome e Foto pro Firestore tudo de uma vez
                db.collection("users").document(emailUsuario)
                    .set(dadosAtualizados, SetOptions.merge())
                    .addOnSuccessListener {

                        // 5. Por último, resolve a senha (se o usuário digitou alguma)
                        if (novaSenha.isNotEmpty()) {
                            if (novaSenha.length >= 6) {
                                user.updatePassword(novaSenha).addOnCompleteListener { senhaTask ->
                                    if (senhaTask.isSuccessful) {
                                        Toast.makeText(this, "Perfil e senha atualizados!", Toast.LENGTH_SHORT).show()
                                        finish()
                                    } else {
                                        Toast.makeText(this, "Perfil salvo, mas houve erro na senha.", Toast.LENGTH_LONG).show()
                                        finish()
                                    }
                                }
                            } else {
                                Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // Se não digitou senha, encerra com sucesso!
                            Toast.makeText(this, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Erro ao salvar dados no Firestore", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Erro ao atualizar nome na conta (Auth)", Toast.LENGTH_SHORT).show()
            }
        }
    }
}