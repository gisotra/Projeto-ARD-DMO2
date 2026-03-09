package br.com.ifsp.gisotra.microredesocial

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.com.ifsp.gisotra.microredesocial.databinding.ActivityHomeBinding
import br.com.ifsp.gisotra.microredesocial.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupListeners()
    }

    fun sairDaActivity(){
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    fun setupListeners(){
        binding.btnLogin.setOnClickListener{(sairDaActivity())}

    }
}