package com.laam.laammedia.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.GsonBuilder
import com.laam.laammedia.R
import com.laam.laammedia.adapters.CommentRecyclerViewAdapter
import com.laam.laammedia.adapters.HomeRecyclerViewAdapter
import com.laam.laammedia.models.Comment
import com.laam.laammedia.models.Post
import com.laam.laammedia.services.api.PostService
import com.laam.laammedia.services.api.ServiceBuilder
import com.laam.laammedia.services.SharedPrefHelper
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_post.*
import kotlinx.android.synthetic.main.toolbar_activity.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostActivity : AppCompatActivity() {
    private var mSocket: Socket = IO.socket(ServiceBuilder.URL)
    private lateinit var mAdapter: CommentRecyclerViewAdapter

    private var post_id: Int = 0
    private var pref_id: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        post_id = intent.getIntExtra("post_id", 0)
        pref_id = SharedPrefHelper(this@PostActivity).getAccount().id

        toolbar_activity_title.text = "Post"
        toolbar_activity_back.setOnClickListener { view ->
            onBackPressed()
        }

        mAdapter = CommentRecyclerViewAdapter(
            arrayListOf(),
            this@PostActivity
        )

        onComment()
        showData()

        a_post_swipe_refresh.setOnRefreshListener {
            showData()
        }
    }

    private fun onComment() {
        mSocket.connect()
        mSocket.emit("joinComment", post_id)
        mSocket.on("initComment", Emitter.Listener {
            runOnUiThread(Runnable {
                home_ori_recyclerview_comment.setHasFixedSize(true)
                home_ori_recyclerview_comment.layoutManager =
                    LinearLayoutManager(this@PostActivity)
                mAdapter = CommentRecyclerViewAdapter(
                    GsonBuilder().create().fromJson(it[0].toString(), Array<Comment>::class.java)
                        .toMutableList(),
                    this@PostActivity
                )
                home_ori_recyclerview_comment.adapter = mAdapter
                home_ori_tv_comment.text = "${mAdapter.itemCount} comment"
            })
        })

        a_profile_btn_send.setOnClickListener {
            val obj: JSONObject = JSONObject()
            obj.put("user_id", pref_id)
            obj.put("post_id", post_id)
            obj.put("content", a_profile_et_send.text)

            mSocket.emit("newComment", obj)
            a_profile_et_send.setText("")
        }

        mSocket.on("newComment", Emitter.Listener {
            runOnUiThread(Runnable {
                mAdapter.updateData(GsonBuilder().create().fromJson(it[0].toString(), Comment::class.java))
                home_ori_tv_comment.text = "${mAdapter.itemCount} comment"
                home_ori_recyclerview_comment.smoothScrollToPosition(mAdapter.itemCount - 1)
                home_ori_recyclerview_comment.scrollTo(0, mAdapter.itemCount - 1)
            })
        })
    }

    private fun showData() {
        recyclerViewPost()
    }

    private fun recyclerViewPost() {
        a_post_swipe_refresh.isRefreshing = true

        val service = ServiceBuilder.buildService(PostService::class.java)
            .getPostByID(pref_id, post_id)
        service.enqueue(object : Callback<List<Post>> {
            override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                if (response.isSuccessful) {
                    home_ori_recyclerview.setHasFixedSize(true)
                    home_ori_recyclerview.layoutManager = LinearLayoutManager(this@PostActivity)
                    home_ori_recyclerview.adapter = HomeRecyclerViewAdapter(
                        response.body()!!,
                        this@PostActivity,
                        supportFragmentManager
                    )
                    a_post_swipe_refresh.isRefreshing = false
                } else {
                    Toast.makeText(this@PostActivity, "Error", Toast.LENGTH_SHORT).show()
                    a_post_swipe_refresh.isRefreshing = false
                }
            }

            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                Toast.makeText(this@PostActivity, "Error : ${t.message}", Toast.LENGTH_SHORT)
                    .show()
                a_post_swipe_refresh.isRefreshing = false
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        mSocket.disconnect()
    }
}
