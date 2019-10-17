package com.laam.laammedia.adapters

import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.laam.laammedia.R

import com.laam.laammedia.models.Post
import com.laam.laammedia.services.api.PostService
import com.laam.laammedia.services.api.ServiceBuilder
import com.laam.laammedia.services.SharedPrefHelper
import com.like.LikeButton
import com.like.OnLikeListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.laam.laammedia.models.response.ResponseLikePost
import org.json.JSONObject
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.laam.laammedia.ui.activities.ProfileActivity
import androidx.fragment.app.FragmentManager
import com.laam.laammedia.ui.activities.PostActivity

class HomeRecyclerViewAdapter(
    private val mValues: List<Post>,
    private val mContext: Context,
    private val mFragmentManager: FragmentManager
) :
    RecyclerView.Adapter<HomeRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext)
            .inflate(R.layout.item_post, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: Post = mValues[position]

        val requestOptions = RequestOptions().transform(CenterCrop(), RoundedCorners(16))
        Glide.with(holder.itemView.context)
            .load("${ServiceBuilder.BASE_URL}${item.imageUrl}")
            .apply(requestOptions)
            .into(holder.img)

        holder.tvTitle.text = item.title
        holder.tvContent.text = item.content
        holder.tvAuthor.text = item.authorName
        holder.tvDate.text = item.createdAt
        holder.tvCount.text = item.likeCount.toString()
        holder.lbLove.isLiked = item.liked == 1

        holder.tvAuthor.setOnClickListener { view ->
            val intent = Intent(mContext.applicationContext, ProfileActivity::class.java)
            intent.putExtra("user_id", item.authorID)
            intent.putExtra("user_name", item.authorName)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            mContext.startActivity(intent)
        }

        holder.imgComment.setOnClickListener { view ->
            val intent = Intent(mContext.applicationContext, PostActivity::class.java)
            intent.putExtra("post_id", item.id)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            mContext.startActivity(intent)
        }

        holder.lbLove.setOnLikeListener(object : OnLikeListener {
            override fun liked(likeButton: LikeButton?) {
                val pref = SharedPrefHelper(mContext)

                if (!pref.isLoggedIn()) {
                    Toast.makeText(mContext, "Ensure you have login", Toast.LENGTH_LONG).show()
                    return
                } else {
                    like_post(holder.tvCount, item.id)
                }
            }

            override fun unLiked(likeButton: LikeButton?) {
                val pref = SharedPrefHelper(mContext)

                if (!pref.isLoggedIn()) {
                    Toast.makeText(mContext, "Ensure you have login", Toast.LENGTH_LONG).show()
                    return
                } else {
                    unlike_post(holder.tvCount, item.id)
                }
            }
        })
    }

    private fun like_post(tvCount: TextView, post_id: Int) {
        val pref = SharedPrefHelper(mContext)

        val service =
            ServiceBuilder.buildService(PostService::class.java)
                .postLike(pref.getAccount().id, post_id)

        service.enqueue(object : Callback<ResponseLikePost> {
            override fun onResponse(
                call: Call<ResponseLikePost>,
                responseLikePost: Response<ResponseLikePost>
            ) {
                if (responseLikePost.isSuccessful) {
                    tvCount.text = responseLikePost.body()!!.message.likeCount.toString()
                } else {
                    try {
                        Toast.makeText(
                            mContext,
                            JSONObject(responseLikePost.errorBody()!!.string()).getString("message"),
                            Toast.LENGTH_LONG
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(mContext, e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onFailure(call: Call<ResponseLikePost>, t: Throwable) {
                Toast.makeText(mContext, "Error : ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun unlike_post(tvCount: TextView, post_id: Int) {
        val pref = SharedPrefHelper(mContext)

        val service =
            ServiceBuilder.buildService(PostService::class.java)
                .deleteLike(pref.getAccount().id, post_id)

        service.enqueue(object : Callback<ResponseLikePost> {
            override fun onResponse(
                call: Call<ResponseLikePost>,
                responseLikePost: Response<ResponseLikePost>
            ) {
                if (responseLikePost.isSuccessful) {
                    tvCount.text = responseLikePost.body()!!.message.likeCount.toString()
                } else {
                    try {
                        Toast.makeText(
                            mContext,
                            JSONObject(responseLikePost.errorBody()!!.string()).getString("message"),
                            Toast.LENGTH_LONG
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(mContext, e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onFailure(call: Call<ResponseLikePost>, t: Throwable) {
                Toast.makeText(mContext, "Error : ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        var img: ImageView = itemView.findViewById(R.id.item_post_iv_photo)
        var imgComment: ImageView = itemView.findViewById(R.id.item_post_img_comment)
        var tvTitle: TextView = itemView.findViewById(R.id.item_post_tv_title)
        var tvContent: TextView = itemView.findViewById(R.id.item_post_tv_content)
        var tvAuthor: TextView = itemView.findViewById(R.id.item_post_tv_author)
        var tvDate: TextView = itemView.findViewById(R.id.item_post_tv_createdat)
        var tvCount: TextView = itemView.findViewById(R.id.item_post_tv_like_count)
        var lbLove: com.like.LikeButton = itemView.findViewById(R.id.item_post_lb_love)
    }
}
