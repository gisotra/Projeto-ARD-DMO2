package br.com.ifsp.gisotra.microredesocial.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.ifsp.gisotra.microredesocial.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupFirebase()
        setupListeners()
    }


    fun setupFirebase(){
        firebaseAuth = FirebaseAuth.getInstance()
    }

    fun autenticarUsuario(){
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()

        firebaseAuth
            .signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (firebaseAuth.currentUser != null) {
                        startActivity(Intent(this, ProfileActivity::class.java))
                        finish()
                    }
                } else {
                    Toast.makeText(this, "Erro no login", Toast.LENGTH_LONG).show()
                }
            }
    }

    fun criarUsuario(){
        startActivity(Intent(this, CadastroActivity::class.java))
        finish()
    }

    fun setupListeners(){
        binding.btnLogin.setOnClickListener{(autenticarUsuario())}
        binding.btnCreateUser.setOnClickListener{(criarUsuario())}
    }
}