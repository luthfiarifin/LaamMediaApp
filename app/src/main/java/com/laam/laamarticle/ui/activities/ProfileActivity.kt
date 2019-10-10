package com.laam.laamarticle.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.laam.laamarticle.R
import com.laam.laamarticle.adapters.ProfileRecyclerViewAdapter
import com.laam.laamarticle.models.Post
import com.laam.laamarticle.models.response.ResponseDB
import com.laam.laamarticle.models.User
import com.laam.laamarticle.services.api.PostService
import com.laam.laamarticle.services.api.ServiceBuilder
import com.laam.laamarticle.services.SharedPrefHelper
import com.laam.laamarticle.services.api.UserService
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.toolbar_activity.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        user_id = intent.getIntExtra("user_id", 0)
        pref_id = SharedPrefHelper(applicationContext).getAccount().id

        profile_btn_edit_profile.visibility = if (pref_id == user_id) {
            View.VISIBLE
        } else {
            View.GONE
        }
        profile_btn_following.visibility = if (pref_id != user_id) {
            View.VISIBLE
        } else {
            View.GONE
        }
        profile_btn_message.visibility = if (pref_id != user_id) {
            View.VISIBLE
        } else {
            View.GONE
        }

        toolbar_activity_title.text = "Profile"
        toolbar_activity_back.setOnClickListener {
            onBackPressed()
        }


        showData()
        recyclerViewData()

        a_profile_swipe_refresh.setOnRefreshListener {
            recyclerViewData()
        }

        btnFollowing()
        btnMessage()
    }

    private fun btnMessage() {
        profile_btn_message.setOnClickListener {
            val intentN = Intent(this@ProfileActivity, DirrectMessageActivity::class.java)
            intentN.putExtra("destination_id", user_id)
            intentN.putExtra("destination_name", intent.getStringExtra("user_name"))
            startActivity(intentN)
        }
    }

    private fun btnFollowing() {
        profile_btn_following.setOnClickListener {
            Log.d("mantap", "Pref ID : $pref_id \nUser ID : $user_id")
            if (!isFollowing) {
                ServiceBuilder.buildService(UserService::class.java).postFollowing(pref_id, user_id)
                    .enqueue(object : Callback<ResponseDB> {
                        override fun onResponse(
                            call: Call<ResponseDB>,
                            response: Response<ResponseDB>
                        ) {
                            if (response.isSuccessful) {
                                Toast.makeText(
                                    this@ProfileActivity,
                                    "${response.body()!!.message}",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                                showData()
                                isFollowing = true
                            } else {
                                Toast.makeText(this@ProfileActivity, "Error", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }

                        override fun onFailure(call: Call<ResponseDB>, t: Throwable) {
                            Toast.makeText(
                                this@ProfileActivity,
                                "Error : ${t.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("onFailure foll", t.message)
                        }
                    })
            } else {
                ServiceBuilder.buildService(UserService::class.java)
                    .deleteUnfollowing(pref_id, user_id)
                    .enqueue(object : Callback<ResponseDB> {
                        override fun onResponse(
                            call: Call<ResponseDB>,
                            response: Response<ResponseDB>
                        ) {
                            if (response.isSuccessful) {
                                Toast.makeText(
                                    this@ProfileActivity,
                                    "${response.body()!!.message}",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                                showData()
                                isFollowing = false
                            } else {
                                Toast.makeText(this@ProfileActivity, "Error", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }

                        override fun onFailure(call: Call<ResponseDB>, t: Throwable) {
                            Toast.makeText(
                                this@ProfileActivity,
                                "Error : ${t.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("onFailure unfoll", t.localizedMessage)
                        }
                    })
            }
        }
    }

    private fun showData() {
        ServiceBuilder.buildService(UserService::class.java).getUserByID(pref_id, user_id)
            .enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful) {
                        val user: User = response.body()!!
                        profile_tv_name.text = user.name
                        profile_tv_description.text = user.bio
                        profile_tv_job_category.text = user.jobCategory
                        profile_count_posts.text = user.postCount
                        profile_count_followers.text = user.followerCount
                        profile_count_following.text = user.followingCount

                        Glide.with(this@ProfileActivity)
                            .load(ServiceBuilder.BASE_URL + user.imageUrl)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .circleCrop()
                            .into(profile_img_user)

                        val userFollowing = user.following == 1
                        Log.d("mantap", "following db : ${user.following}")
                        profile_btn_following.setBackgroundDrawable(
                            if (userFollowing) {
                                resources.getDrawable(R.drawable.shape_button_profile)
                            } else {
                                resources.getDrawable(R.drawable.shape_button_profile_before)
                            }
                        )
                        profile_btn_following.text = if (userFollowing) {
                            "Following"
                        } else {
                            "Follow"
                        }
                        profile_btn_following.setTextColor(
                            if (userFollowing) {
                                resources.getColor(R.color.textBlack)
                            } else {
                                resources.getColor(R.color.colorWhite)
                            }
                        )
                        isFollowing = userFollowing


                    } else {
                        Toast.makeText(applicationContext, "Error", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    Toast.makeText(applicationContext, "Error : ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                    Log.e("onFailure ${application.packageName}", t.localizedMessage!!)
                }
            })
    }

    private var user_id: Int = 0
    private var pref_id: Int = 0
    private var isFollowing: Boolean = false

    private fun recyclerViewData() {
        a_profile_swipe_refresh.isRefreshing = true

        val service = ServiceBuilder.buildService(PostService::class.java)
            .getPostProfile(user_id)
        service.enqueue(object : Callback<List<Post>> {
            override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                if (response.isSuccessful) {
                    if (response.body()!!.isEmpty()) {
                        profile_layout_not_post.visibility = View.VISIBLE
                        profile_btn_add.visibility = View.GONE
                    } else {
                        profile_layout_not_post.visibility = View.GONE

                        profile_recyclerview.layoutManager =
                            GridLayoutManager(applicationContext, 3)
                        val adapter = ProfileRecyclerViewAdapter(
                            response.body()!!,
                            applicationContext
                        )
                        profile_recyclerview.adapter = adapter
                    }
                    a_profile_swipe_refresh.isRefreshing = false
                } else {
                    Toast.makeText(applicationContext, "Error", Toast.LENGTH_SHORT).show()
                    a_profile_swipe_refresh.isRefreshing = false
                }
            }

            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                Toast.makeText(applicationContext, "Error : ${t.message}", Toast.LENGTH_SHORT)
                    .show()
                a_profile_swipe_refresh.isRefreshing = false
            }
        })
    }
}
