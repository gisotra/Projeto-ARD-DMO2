package br.com.ifsp.gisotra.microredesocial.data.model

import android.graphics.Bitmap
import com.google.firebase.Timestamp

data class Post (
    var descricao: String = "",
    var imagem: String = "",
    var data: Timestamp = Timestamp.now(),
    var nomeAutor: String = "",
    var cidade: String = ""
)