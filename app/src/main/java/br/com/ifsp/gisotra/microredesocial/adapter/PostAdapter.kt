package br.com.ifsp.gisotra.microredesocial.adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.com.ifsp.gisotra.microredesocial.data.model.Post
import br.com.ifsp.gisotra.microredesocial.databinding.ItemPostBinding

class PostAdapter(private val posts: MutableList<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        // Inflamos o layout item_post.xml
        val binding = ItemPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(binding)
    }

    class PostViewHolder(val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        holder.binding.txtDescricao.text = post.descricao

        // Se você tiver adicionado esses campos no item_post.xml, pode descomentar:
        // holder.binding.txtNomeAutor.text = post.nomeAutor
        // holder.binding.txtCidade.text = post.cidade

        // Decodificando o Base64 de volta para Imagem (Bitmap)
        try {
            val imageBytes = Base64.decode(post.imagem, Base64.DEFAULT)
            val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            holder.binding.imgPost.setImageBitmap(decodedImage)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        holder.itemView.setOnClickListener {
            val intent = android.content.Intent(holder.itemView.context, br.com.ifsp.gisotra.microredesocial.ui.PostActivity::class.java)
            intent.putExtra("descricao", post.descricao)
            intent.putExtra("autor", post.nomeAutor)
            intent.putExtra("cidade", post.cidade)
            intent.putExtra("imagem", post.imagem)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = posts.size

    fun adicionarPosts(novosPosts: List<Post>) {
        val inicio = posts.size
        posts.addAll(novosPosts)
        notifyItemRangeInserted(inicio, novosPosts.size)
    }
}