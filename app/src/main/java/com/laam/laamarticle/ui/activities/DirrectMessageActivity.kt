package com.laam.laamarticle.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.GsonBuilder
import com.laam.laamarticle.R
import com.laam.laamarticle.adapters.MessageRecyclerViewAdapter
import com.laam.laamarticle.models.DirrectMessage
import com.laam.laamarticle.models.Message
import com.laam.laamarticle.services.SharedPrefHelper
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_dirrect_message.*
import kotlinx.android.synthetic.main.activity_post.*
import kotlinx.android.synthetic.main.toolbar_activity.*
import org.json.JSONObject

class DirrectMessageActivity : AppCompatActivity() {
    private var mSocket: Socket = IO.socket("http://10.0.2.2:3003/")
    private var mAdapter: MessageRecyclerViewAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dirrect_message)

        destination_id = intent.getIntExtra("destination_id", 0)
        pref_id = SharedPrefHelper(this@DirrectMessageActivity).getAccount().id

        toolbar_activity_title.text = intent.getStringExtra("destination_name")
        toolbar_activity_back.setOnClickListener {
            onBackPressed()
        }

        mAdapter = MessageRecyclerViewAdapter(
            arrayListOf(),
            this@DirrectMessageActivity
        )

        onMessage()
    }

    private fun onMessage() {
        val objJoin: JSONObject = JSONObject()
        objJoin.put("user_id", pref_id)
        objJoin.put("destination_id", destination_id)

        mSocket.connect()
        mSocket.emit("joinMessage", objJoin)
        mSocket.on("initMessage", Emitter.Listener {
            runOnUiThread(Runnable {
                dm_recyclerview_message.setHasFixedSize(true)
                dm_recyclerview_message.layoutManager =
                    LinearLayoutManager(this@DirrectMessageActivity)
                mAdapter = MessageRecyclerViewAdapter(
                    GsonBuilder().create().fromJson(
                        it[0].toString(),
                        Array<DirrectMessage>::class.java
                    )
                        .toMutableList(),
                    this@DirrectMessageActivity
                )
                dm_recyclerview_message.adapter = mAdapter
                dm_recyclerview_message.scrollToPosition(mAdapter!!.itemCount - 1)
                dm_recyclerview_message.scrollTo(0, mAdapter!!.itemCount - 1)
            })
        })

        dm_btn_send.setOnClickListener {
            if (!dm_et_send.text.toString().trim().equals("")) {
                val obj: JSONObject = JSONObject()
                obj.put("user_id", pref_id)
                obj.put("destination_id", destination_id)
                obj.put("content", dm_et_send.text)

                mSocket.emit("newMessage", obj)
                dm_et_send.setText("")
            }
        }

        mSocket.on("newMessage", Emitter.Listener {
            runOnUiThread(Runnable {
                mAdapter!!.updateData(
                    GsonBuilder().create().fromJson(
                        it[0].toString(),
                        DirrectMessage::class.java
                    )
                )
                dm_recyclerview_message.smoothScrollToPosition(mAdapter!!.itemCount - 1)
                dm_recyclerview_message.scrollTo(0, mAdapter!!.itemCount - 1)
            })
        })
    }

    private var pref_id: Int = 0
    private var destination_id: Int = 0

    override fun onDestroy() {
        super.onDestroy()

        mSocket.disconnect()
    }
}
