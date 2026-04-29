package br.com.ifsp.gisotra.microredesocial.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import br.com.ifsp.gisotra.microredesocial.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
    class MainActivity : AppCompatActivity() {
        private lateinit var binding : ActivityMainBinding
        private lateinit var auth: FirebaseAuth

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            auth = FirebaseAuth.getInstance()



            setupListeners()
        }

        override fun onStart(){
            super.onStart()

            if (auth.currentUser != null) {
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
        }

        private fun setupListeners() {
            // IDs que você precisa ter no seu activity_main.xml: btnLogin, btnCriarConta, etEmail, etSenha
            binding.btnLogin.setOnClickListener { fazerLogin() }

            binding.btnCreateUser.setOnClickListener {
                startActivity(Intent(this, CadastroActivity::class.java))
            }
        }


        private fun fazerLogin() {
            val email = binding.etEmail.text.toString().trim()
            val senha = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && senha.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, senha)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            startActivity(Intent(this, HomeActivity::class.java))
                        } else {
                            Toast.makeText(this, "Erro ao logar: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Preencha e-mail e senha", Toast.LENGTH_SHORT).show()
            }
        }

    }
