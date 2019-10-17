package com.laam.laammedia.adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.laam.laammedia.R
import com.laam.laammedia.models.Category

class TagRecyclerViewAdapter(
    private val mValues: List<Category>,
    private val mContext: Context
) :
    RecyclerView.Adapter<TagRecyclerViewAdapter.ViewHolder>() {

    private lateinit var onItemClickCallback: OnItemClickCallback
    private var row_index: Int = 0

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext)
            .inflate(R.layout.item_tag, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: Category = mValues[position]

        holder.btnName.text = item.name
        holder.btnName.setOnClickListener {
            row_index = position
            onItemClickCallback.onItemClicked(item)
            notifyDataSetChanged()
        }

        holder.btnName.setBackgroundDrawable(
            if (row_index == position) {
                mContext.resources.getDrawable(R.drawable.shape_tag_clicked)
            } else {
                mContext.resources.getDrawable(R.drawable.shape_tag)
            }
        )

        holder.btnName.setTextColor(
            if (row_index == position) {
                mContext.resources.getColor(R.color.colorWhite)
            } else {
                mContext.resources.getColor(R.color.textBlack)
            }
        )
    }


    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) :
        RecyclerView.ViewHolder(mView) {

        var btnName: Button = itemView.findViewById(R.id.item_tag_name)
    }

    interface OnItemClickCallback {
        fun onItemClicked(data: Category)
    }
}