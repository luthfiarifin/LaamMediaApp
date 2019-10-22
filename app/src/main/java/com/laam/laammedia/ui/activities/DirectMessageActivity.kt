package com.laam.laammedia.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.GsonBuilder
import com.laam.laammedia.R
import com.laam.laammedia.adapters.MessageRecyclerViewAdapter
import com.laam.laammedia.models.DirrectMessage
import com.laam.laammedia.services.SharedPrefHelper
import com.laam.laammedia.services.api.ServiceBuilder
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_dirrect_message.*
import kotlinx.android.synthetic.main.toolbar_activity.*
import org.json.JSONObject

class DirectMessageActivity : AppCompatActivity() {
    private var mSocket: Socket = IO.socket(ServiceBuilder.URL)
    private var mAdapter: MessageRecyclerViewAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dirrect_message)

        destination_id = intent.getIntExtra("destination_id", 0)
        pref_id = SharedPrefHelper(this@DirectMessageActivity).getAccount().id

        toolbar_activity_title.text = intent.getStringExtra("destination_name")
        toolbar_activity_back.setOnClickListener {
            onBackPressed()
        }

        mAdapter = MessageRecyclerViewAdapter(
            arrayListOf(),
            this@DirectMessageActivity
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
                    LinearLayoutManager(this@DirectMessageActivity)
                mAdapter = MessageRecyclerViewAdapter(
                    GsonBuilder().create().fromJson(
                        it[0].toString(),
                        Array<DirrectMessage>::class.java
                    )
                        .toMutableList(),
                    this@DirectMessageActivity
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
                if (mAdapter!!.itemCount <= 1) {
                    finish()
                    startActivity(
                        Intent(
                            this@DirectMessageActivity,
                            DirectMessageActivity::class.java
                        ).putExtra("destination_id", destination_id)
                            .putExtra("destination_name", toolbar_activity_title.text)
                    )
                }
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
