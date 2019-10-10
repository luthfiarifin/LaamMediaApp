package com.laam.laamarticle.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.laam.laamarticle.R
import com.laam.laamarticle.adapters.CommentRecyclerViewAdapter
import com.laam.laamarticle.adapters.HeaderMessageRecyclerViewAdapter
import com.laam.laamarticle.models.Comment
import com.laam.laamarticle.models.HeaderMessage
import com.laam.laamarticle.services.api.PostService
import com.laam.laamarticle.services.api.ServiceBuilder
import com.laam.laamarticle.services.SharedPrefHelper
import kotlinx.android.synthetic.main.activity_header_message.*
import kotlinx.android.synthetic.main.activity_post.*
import kotlinx.android.synthetic.main.toolbar_activity.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HeaderMessageActivity : AppCompatActivity() {
    private var pref_id: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_header_message)

        pref_id = SharedPrefHelper(this@HeaderMessageActivity).getAccount().id

        toolbar_activity_title.text = "Message"
        toolbar_activity_back.setOnClickListener { view ->
            onBackPressed()
        }

        showData()

        header_message_swipe_refresh.setOnRefreshListener {
            showData()
        }

        header_message_floating_add.setOnClickListener {
            startActivity(Intent(this@HeaderMessageActivity, NewMessageActivity::class.java))
        }
    }


    private fun showData() {
        header_message_swipe_refresh.isRefreshing = true

        val service = ServiceBuilder.buildService(PostService::class.java)
            .getHeaderMessage(pref_id)
        service.enqueue(object : Callback<List<HeaderMessage>> {
            override fun onResponse(
                call: Call<List<HeaderMessage>>,
                response: Response<List<HeaderMessage>>
            ) {
                header_message_recyclerview.setHasFixedSize(true)
                header_message_recyclerview.layoutManager =
                    LinearLayoutManager(this@HeaderMessageActivity)
                header_message_recyclerview.adapter =
                    HeaderMessageRecyclerViewAdapter(response.body()!!, this@HeaderMessageActivity)
                header_message_swipe_refresh.isRefreshing = false
            }

            override fun onFailure(call: Call<List<HeaderMessage>>, t: Throwable) {
                Log.e("onFailure", t.message)
                header_message_swipe_refresh.isRefreshing = false
            }

        })

    }

    override fun onResume() {
        super.onResume()
        showData()
    }
}
