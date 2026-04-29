package br.com.ifsp.gisotra.microredesocial.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.ifsp.gisotra.microredesocial.databinding.ActivityCadastroBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore // IMPORT NOVO

class CadastroActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCadastroBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore // VARIÁVEL DO FIRESTORE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupFirebase()
        setupListeners()
    }

    private fun setupFirebase() {
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance() // INICIALIZANDO O FIRESTORE
    }

    private fun setupListeners() {
        binding.btnCriar.setOnClickListener { criarUsuario() }
    }

    private fun criarUsuario() {
        val nome = binding.etNome.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmaPassword = binding.etPasswordValidation.text.toString().trim()

        if (nome.isEmpty() || email.isEmpty() || password.isEmpty() || confirmaPassword.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmaPassword) {
            Toast.makeText(this, "As senhas não coincidem!", Toast.LENGTH_SHORT).show()
            return
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser

                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(nome)
                        .build()

                    user?.updateProfile(profileUpdates)?.addOnCompleteListener {

                        val userData = hashMapOf(
                            "nome" to nome,
                            "email" to email,
                            "foto" to ""
                        )

                        db.collection("users").document(email)
                            .set(userData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, HomeActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Erro ao salvar dados no banco: ${e.message}", Toast.LENGTH_LONG).show()
                                startActivity(Intent(this, HomeActivity::class.java))
                                finish()
                            }
                    }
                } else {
                    Toast.makeText(this, "Erro: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}