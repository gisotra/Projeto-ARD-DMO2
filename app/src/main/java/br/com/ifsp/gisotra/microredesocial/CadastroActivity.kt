package br.com.ifsp.gisotra.microredesocial

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.com.ifsp.gisotra.microredesocial.databinding.ActivityCadastroBinding
import br.com.ifsp.gisotra.microredesocial.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class CadastroActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCadastroBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cadastro)
        setupFirebase()
        setupListeners()
    }

    fun setupFirebase(){
        firebaseAuth = FirebaseAuth.getInstance()



    }

    fun criarUsuario(){
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        val confirmar = binding.etPasswordValidation.text.toString()

        if(email.isEmpty() || password.isEmpty() || confirmar.isEmpty()){
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_LONG).show()
        }

        if(password != confirmar){
            Toast.makeText(this, "Senha digitada errada", Toast.LENGTH_LONG).show()
        }

        firebaseAuth
            .signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (firebaseAuth.currentUser != null) {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                } else {
                    Toast.makeText(this, "Erro na criação", Toast.LENGTH_LONG).show()
                }
            }
    }

    fun setupListeners(){
        binding.btnCriar.setOnClickListener{(criarUsuario())}

    }

}