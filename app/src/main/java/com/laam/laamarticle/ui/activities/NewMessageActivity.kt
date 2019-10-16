package com.laam.laamarticle.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.laam.laamarticle.R
import com.laam.laamarticle.adapters.HeaderMessageRecyclerViewAdapter
import com.laam.laamarticle.models.HeaderMessage
import com.laam.laamarticle.services.api.PostService
import com.laam.laamarticle.services.api.ServiceBuilder
import com.laam.laamarticle.services.SharedPrefHelper
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.toolbar_activity.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NewMessageActivity : AppCompatActivity() {
    private var pref_id: Int = 0
    private var search: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        pref_id = SharedPrefHelper(this@NewMessageActivity).getAccount().id

        toolbar_activity_title.text = "Following User"
        toolbar_activity_back.setOnClickListener { view ->
            onBackPressed()
        }

        showData()

        new_message_swipe_refresh.setOnRefreshListener {
            showData()
        }

        new_message_search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                search = p0.toString()
                showData()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

        new_message_btn_following.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK))
        }
    }


    private fun showData() {
        new_message_swipe_refresh.isRefreshing = true

        val service = ServiceBuilder.buildService(PostService::class.java)
            .getListFollowing(pref_id, search)
        service.enqueue(object : Callback<List<HeaderMessage>> {
            override fun onResponse(
                call: Call<List<HeaderMessage>>,
                response: Response<List<HeaderMessage>>
            ) {
                if (response.body()!!.isEmpty()) {
                    new_message_layout_not_found.visibility = View.VISIBLE
                } else {
                    new_message_layout_not_found.visibility = View.GONE
                }
                new_message_recyclerview.setHasFixedSize(true)
                new_message_recyclerview.layoutManager =
                    LinearLayoutManager(this@NewMessageActivity)
                new_message_recyclerview.adapter =
                    HeaderMessageRecyclerViewAdapter(response.body()!!, this@NewMessageActivity)
                new_message_swipe_refresh.isRefreshing = false
            }

            override fun onFailure(call: Call<List<HeaderMessage>>, t: Throwable) {
                Log.e("onFailure", t.message)
                new_message_swipe_refresh.isRefreshing = false
            }

        })

    }

    override fun onResume() {
        super.onResume()
        showData()
    }
}
