package com.laam.laamarticle.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.laam.laamarticle.R
import com.laam.laamarticle.models.Post
import com.laam.laamarticle.services.api.ServiceBuilder
import com.laam.laamarticle.ui.activities.PostActivity

class ProfileRecyclerViewAdapter(private val mValues: List<Post>, private val mContext: Context) :
    RecyclerView.Adapter<ProfileRecyclerViewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProfileRecyclerViewAdapter.ViewHolder {
        val inflater = LayoutInflater.from(mContext).inflate(R.layout.item_profile, parent, false)
        return ViewHolder(inflater)
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    override fun onBindViewHolder(holder: ProfileRecyclerViewAdapter.ViewHolder, position: Int) {
        val item = mValues[position]

        val requestOptions = RequestOptions().transform(CenterCrop(), RoundedCorners(7))
        Glide.with(holder.itemView.context)
            .load(ServiceBuilder.BASE_URL + item.imageUrl)
            .apply(requestOptions)
            .into(holder.img)

        holder.img.setOnClickListener { view ->
            val intent = Intent(mContext.applicationContext, PostActivity::class.java)
            intent.putExtra("post_id", item.id)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            mContext.startActivity(intent)
        }
    }

    class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val img: ImageView = mView.findViewById(R.id.item_profile_image)
    }
}