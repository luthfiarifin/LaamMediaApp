package com.laam.laamarticle.ui.fragments

import android.content.Intent
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
import com.laam.laamarticle.ui.activities.AddPostActivity
import com.laam.laamarticle.ui.activities.LoginActivity
import com.laam.laamarticle.ui.activities.RegisterActivity
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.toolbar_activity.toolbar_activity_back
import kotlinx.android.synthetic.main.toolbar_activity.toolbar_activity_title
import kotlinx.android.synthetic.main.toolbar_activity_post.*
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
        setText()

        toolbar_activity_title.text = "Profile"
        toolbar_activity_share.text = "Logout"
        toolbar_activity_back.visibility = View.GONE
        toolbar_activity_share.setOnClickListener {
            onLogOutPressed()
        }

        profile_swipe_refresh.setOnRefreshListener {
            recyclerViewData()
        }

        profile_btn_add.setOnClickListener {
            onPostAddPressed()
        }

        profile_btn_edit_profile.setOnClickListener {
            onEditProfilePressed()
        }
    }

    private fun onEditProfilePressed() {
        activity!!.startActivity(
            Intent(activity!!, RegisterActivity::class.java).putExtra(
                "isEdit",
                true
            )
        )
    }

    private fun onPostAddPressed() {
        activity!!.startActivity(Intent(activity!!, AddPostActivity::class.java))
    }

    private fun onLogOutPressed() {
        val pref = SharedPrefHelper(activity!!)
        pref.clearUser()
        startActivity(
            Intent(
                activity!!,
                LoginActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
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
