package com.laam.laammedia.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.laam.laammedia.R
import com.laam.laammedia.models.Comment
import com.laam.laammedia.services.api.ServiceBuilder

class CommentRecyclerViewAdapter(
    private val mValues: MutableList<Comment>,
    private val mContext: Context
) :
    RecyclerView.Adapter<CommentRecyclerViewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CommentRecyclerViewAdapter.ViewHolder {
        val inflater = LayoutInflater.from(mContext).inflate(R.layout.item_comment, parent, false)
        return ViewHolder(inflater)
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    override fun onBindViewHolder(holder: CommentRecyclerViewAdapter.ViewHolder, position: Int) {
        val item = mValues[position]

        holder.tvNama.text = item.name
        holder.tvContent.text = item.content
        Glide.with(mContext)
            .load(ServiceBuilder.BASE_URL + item.imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .circleCrop()
            .into(holder.imgUser)
    }

    class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val tvNama: TextView = itemView.findViewById(R.id.item_comment_tv_name)
        val tvContent: TextView = itemView.findViewById(R.id.item_comment_tv_content)
        val imgUser: ImageView = itemView.findViewById(R.id.item_comment_img_profile)
    }

    fun updateData(data: Comment) {
        mValues.add(data)
        if (mValues.size <= 3) {
            notifyDataSetChanged()
        }
        notifyItemInserted(mValues.size - 1)
    }
}