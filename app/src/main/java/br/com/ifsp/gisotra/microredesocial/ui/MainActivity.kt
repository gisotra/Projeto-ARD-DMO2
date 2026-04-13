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
import br.com.ifsp.gisotra.microredesocial.tool.LocalizacaoHelper
import com.google.firebase.auth.FirebaseAuth
import br.com.ifsp.gisotra.microredesocial.tool.LocalizacaoHelper.Callback

    class MainActivity : AppCompatActivity(), LocalizacaoHelper.Callback {
        private lateinit var binding : ActivityMainBinding
        private val LOCATION_PERMISSION_REQUEST_CODE = 1001
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
            binding.btnAtualizar.setOnClickListener {
                solicitarLocalizacao()
            }
        }
        private fun solicitarLocalizacao() {
            if (ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                &&
                ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            } else {
                val localizacaoHelper = LocalizacaoHelper(applicationContext)
                localizacaoHelper.obterLocalizacaoAtual(this)
            }
        }
        override fun onLocalizacaoRecebida(endereco: Address, latitude: Double,
                                           longitude: Double) {
            runOnUiThread {
                var infos = endereco.locality
                infos += "\n" + endereco.subLocality
                infos += "\n" + endereco.adminArea
                infos += "\n" + endereco.subAdminArea
                infos += "\n" + endereco.postalCode
                infos += "\n" + endereco.countryName + ", " + endereco.countryCode
                infos += "\n" + endereco.getAddressLine(0)
                binding.txtCidade.text = infos
                binding.txtLongitudeLatitude.text = "Latitude: $latitude\nLongitude: $longitude"
            }
        }
        override fun onErro(mensagem: String) {
            System.out.println(mensagem)
        }
        override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<out String>, grantResults:
            IntArray
        ) {
            super.onRequestPermissionsResult(requestCode, permissions,
                grantResults)
            if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
                grantResults.size > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                solicitarLocalizacao()
            } else {
                Toast.makeText(this, "Permissão de localização negada",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
