package com.laam.laamarticle.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.laam.laamarticle.R
import com.laam.laamarticle.adapters.ProfileRecyclerViewAdapter
import com.laam.laamarticle.models.Post
import com.laam.laamarticle.models.User
import com.laam.laamarticle.services.api.PostService
import com.laam.laamarticle.services.api.ServiceBuilder
import com.laam.laamarticle.services.SharedPrefHelper
import com.laam.laamarticle.services.api.UserService
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.toolbar_activity.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        visibleAccount()

        profile_recyclerview.setHasFixedSize(true)
        recyclerViewData()
        setData()

        toolbar_activity_title.text = "Profile"
        toolbar_activity_back.visibility = View.GONE

        profile_swipe_refresh.setOnRefreshListener {
            recyclerViewData()
        }
    }

    private fun setData() {
        val pref = SharedPrefHelper(activity!!.applicationContext)
        ServiceBuilder.buildService(UserService::class.java).getUserByID(0, pref.getAccount().id)
            .enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful) {
                        val user_ref = response.body()!!

                        pref.saveUser(User(
                            user_ref.id,
                            user_ref.email,
                            user_ref.password,
                            user_ref.name,
                            user_ref.jobCategory,
                            user_ref.bio,
                            user_ref.imageUrl,
                            user_ref.postCount,
                            user_ref.followerCount,
                            user_ref.followingCount,
                            user_ref.following
                        ))
                    } else {
                        Toast.makeText(activity!!.applicationContext, "Error", Toast.LENGTH_SHORT)
                            .show()
                    }
                    setText()
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    Toast.makeText(
                        activity!!.applicationContext, "Error : ${t.message}", Toast.LENGTH_SHORT
                    )
                        .show()
                    setText()
                }
            })
    }

    private fun setText() {
        val user = SharedPrefHelper(activity!!.applicationContext).getAccount()
        profile_tv_name.text = user.name
        profile_tv_description.text = user.bio
        profile_tv_job_category.text = user.jobCategory
        profile_count_posts.text = user.postCount
        profile_count_followers.text = user.followerCount
        profile_count_following.text = user.followingCount
        Glide.with(activity!!.applicationContext)
            .load(ServiceBuilder.BASE_URL + user.imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .circleCrop()
            .into(profile_img_user)
    }

    private fun visibleAccount() {
        profile_btn_following.visibility = View.GONE
        profile_btn_message.visibility = View.GONE
    }

    private fun recyclerViewData() {
        profile_swipe_refresh.isRefreshing = true
        val pref = SharedPrefHelper(activity!!.applicationContext)
        val service = ServiceBuilder.buildService(PostService::class.java)
            .getPostProfile(pref.getAccount().id)
        service.enqueue(object : Callback<List<Post>> {
            override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                if (response.isSuccessful) {
                    if (response.body()!!.isEmpty()) {
                        profile_layout_not_post.visibility = View.VISIBLE
                        Toast.makeText(activity, "Tes", Toast.LENGTH_SHORT).show()
                    } else {
                        profile_layout_not_post.visibility = View.GONE

                        profile_recyclerview.layoutManager = GridLayoutManager(activity, 3)
                        val adapter = ProfileRecyclerViewAdapter(
                            response.body()!!,
                            activity!!.applicationContext
                        )
                        profile_recyclerview.adapter = adapter
                    }
                } else {
                    Toast.makeText(activity, "Error", Toast.LENGTH_SHORT).show()
                }
                profile_swipe_refresh.isRefreshing = false
            }

            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                Toast.makeText(activity, "Error : ${t.message}", Toast.LENGTH_SHORT).show()
                profile_swipe_refresh.isRefreshing = false
            }
        })
    }

    companion object {
        fun newInstance(): ProfileFragment {
            val fragment = ProfileFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}
