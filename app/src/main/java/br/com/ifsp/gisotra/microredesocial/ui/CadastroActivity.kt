package br.com.ifsp.gisotra.microredesocial.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.ifsp.gisotra.microredesocial.databinding.ActivityCadastroBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class CadastroActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCadastroBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupFirebase()
        setupListeners()
    }

    fun setupFirebase() {
        firebaseAuth = FirebaseAuth.getInstance()
    }

    fun setupListeners() {
        binding.btnCriar.setOnClickListener { criarUsuario() }
    }

    private fun criarUsuario() {
        val nome = binding.etNome.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmaPassword = binding.etPasswordValidation.text.toString().trim()

        // [RF1-2] Validando todos os campos requeridos
        if (nome.isEmpty() || email.isEmpty() || password.isEmpty() || confirmaPassword.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        // Validação extra de senha
        if (password != confirmaPassword) {
            Toast.makeText(this, "As senhas não coincidem!", Toast.LENGTH_SHORT).show()
            return
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Salvar o Nome do usuário no profile do Firebase Auth
                    val user = firebaseAuth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(nome)
                        .build()

                    user?.updateProfile(profileUpdates)?.addOnCompleteListener {
                        Toast.makeText(this, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show()
                        // Já manda pra Home e limpa a pilha de telas
                        startActivity(Intent(this, HomeActivity::class.java))
                        finishAffinity()
                    }
                } else {
                    Toast.makeText(this, "Erro: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }


}