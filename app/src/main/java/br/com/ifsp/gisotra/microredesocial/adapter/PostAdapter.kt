package br.com.ifsp.gisotra.microredesocial.adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.com.ifsp.gisotra.microredesocial.R // Adicionei o import do R para puxar a foto padrão
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

        // 1. Amarra os textos
        holder.binding.txtNomeAutor.text = post.nomeAutor
        holder.binding.txtCidade.text = "📍 ${post.cidade}"
        holder.binding.txtDescricao.text = post.descricao

        // 2. FOTO DO AUTOR (Nova lógica aqui!)
        if (!post.fotoAutor.isNullOrEmpty()) {
            try {
                val imageBytes = Base64.decode(post.fotoAutor, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.binding.imgFotoAutorPost.setImageBitmap(decodedImage)
            } catch (e: Exception) {
                e.printStackTrace()
                // Se der erro ao carregar, põe a imagem padrão
                holder.binding.imgFotoAutorPost.setImageResource(R.drawable.ic_launcher_foreground)
            }
        } else {
            // Se o cara não tiver foto de perfil, põe a imagem padrão
            holder.binding.imgFotoAutorPost.setImageResource(R.drawable.ic_launcher_foreground)
        }

        // 3. FOTO GRANDE DO POST (Sua lógica original)
        if (!post.imagem.isNullOrEmpty()) {
            try {
                val imageBytes = Base64.decode(post.imagem, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.binding.imgPost.setImageBitmap(decodedImage)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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