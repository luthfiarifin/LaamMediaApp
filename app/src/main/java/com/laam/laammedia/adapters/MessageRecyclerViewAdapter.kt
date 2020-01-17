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
import com.laam.laammedia.models.DirrectMessage
import com.laam.laammedia.services.SharedPrefHelper
import com.laam.laammedia.services.api.ServiceBuilder


class MessageRecyclerViewAdapter(
    private val mValues: MutableList<DirrectMessage>,
    private val mContext: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val VIEW_TYPE_MESSAGE_SENT = 1
    private val VIEW_TYPE_MESSAGE_RECEIVED = 2

    override fun getItemViewType(position: Int): Int {
        val item = mValues[position]

        return if (item.userId == SharedPrefHelper(mContext).getAccount().id) {
            VIEW_TYPE_MESSAGE_SENT
        } else {
            VIEW_TYPE_MESSAGE_RECEIVED
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            return MessageRecyclerViewAdapter.ReceivedViewHolder(
                LayoutInflater.from(mContext).inflate(
                    R.layout.item_message_received,
                    parent,
                    false
                )
            )
        } else {
            return MessageRecyclerViewAdapter.SentViewHolder(
                LayoutInflater.from(mContext).inflate(
                    R.layout.item_message_sent,
                    parent,
                    false
                )
            )
        }
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = mValues[position]

        when (holder.itemViewType) {
            VIEW_TYPE_MESSAGE_SENT -> {
                val holderS = holder as SentViewHolder
                holderS.sentMessage.text = item.content
            }
            VIEW_TYPE_MESSAGE_RECEIVED -> {
                val holderR = holder as ReceivedViewHolder
                holderR.destMessage.text = item.content
                Glide.with(mContext)
                    .load(ServiceBuilder.BASE_URL + item.imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .circleCrop()
                    .into(holderR.destPhoto)
            }
        }
    }

    class SentViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val sentMessage: TextView = mView.findViewById(R.id.item_message_sent_message)
    }

    class ReceivedViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val destMessage: TextView = mView.findViewById(R.id.item_message_received_message)
        val destPhoto: ImageView = mView.findViewById(R.id.item_message_received_profile)
    }


    fun updateData(data: DirrectMessage) {
        mValues.add(data)
        notifyItemInserted(mValues.size - 1)
    }
}