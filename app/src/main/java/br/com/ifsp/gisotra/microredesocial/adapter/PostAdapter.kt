package br.com.ifsp.gisotra.microredesocial.adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.com.ifsp.gisotra.microredesocial.data.model.Post
import br.com.ifsp.gisotra.microredesocial.databinding.ItemPostBinding

class PostAdapter(private val listaDePosts: MutableList<Post>) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    // Cria as "cascas" da lista
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    // Preenche as "cascas" com os dados do Firebase
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = listaDePosts[position]

        // 1. Amarra os textos que estavam faltando
        holder.binding.txtNomeAutor.text = post.nomeAutor
        holder.binding.txtCidade.text = "📍 ${post.cidade}"
        holder.binding.txtDescricao.text = post.descricao

        // 2. Transforma o Base64 de volta pra imagem
        if (!post.imagem.isNullOrEmpty()) {
            try {
                val imageBytes = Base64.decode(post.imagem, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.binding.imgPost.setImageBitmap(decodedImage)
            } catch (e: Exception) {
                e.printStackTrace()
                // Aqui você poderia colocar uma imagem de erro padrão se quisesse
            }
        }

        // POST NÃO É MAIS CLICÁVEL! Aquele bloco do setOnClickListener foi apagado daqui.
    }

    override fun getItemCount(): Int {
        return listaDePosts.size
    }

    fun adicionarPosts(novosPosts: List<Post>) {
        val tamanhoAtual = listaDePosts.size
        listaDePosts.addAll(novosPosts)
        notifyItemRangeInserted(tamanhoAtual, novosPosts.size)
    }

    class PostViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root)
}