package br.com.ifsp.gisotra.microredesocial.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.com.ifsp.gisotra.microredesocial.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupListeners()
    }

    fun Entrar(){
        startActivity(Intent(this, ProfileActivity::class.java))
        finish()
    }

    fun setupListeners(){
        binding.btnLogin.setOnClickListener{(Entrar())}


    }
}