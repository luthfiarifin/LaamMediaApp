package com.laam.laammedia.adapters

import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.laam.laammedia.R

import com.laam.laammedia.models.HeaderMessage
import com.laam.laammedia.services.api.ServiceBuilder
import com.laam.laammedia.ui.activities.DirrectMessageActivity

class HeaderMessageRecyclerViewAdapter(
    private val mValues: List<HeaderMessage>,
    private val mContext: Context
) :
    RecyclerView.Adapter<HeaderMessageRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext)
            .inflate(R.layout.item_header_message, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]

        holder.tvName.text = item.destinationName
        holder.tvContent.text = item.content

        Glide.with(mContext)
            .load(ServiceBuilder.BASE_URL + item.imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .circleCrop()
            .into(holder.imgProfie)

        holder.itemView.setOnClickListener {
            val intent = Intent(mContext, DirrectMessageActivity::class.java)

            intent.putExtra("destination_id", item.destinationId)
            intent.putExtra("destination_name", item.destinationName)
            mContext.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = mValues.size
    class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val tvName: TextView = mView.findViewById(R.id.item_header_message_name)
        val tvContent: TextView = mView.findViewById(R.id.item_header_message_content)
        val imgProfie: ImageView = mView.findViewById(R.id.item_header_message_profile)
    }
}
