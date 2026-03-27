package br.com.ifsp.gisotra.microredesocial.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.com.ifsp.gisotra.microredesocial.data.model.Postage

class PostageAdapter (private val posts: Array<Postage>){
    RecyclerView.Adapter<PostageAdapter.PostageViewHolder>() {

        class PostViewHolder(val binding: PostageItemBinding) :
            RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostageViewHolder {
            val binding = PostageItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return PostageViewHolder(binding)
        }

        override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
            val post = posts[position]

            holder.binding.txtDescricao.text = post.descricao
            holder.binding.imgPost.setImageBitmap(post.imagem)
        }

        override fun getItemCount(): Int = posts.size
}