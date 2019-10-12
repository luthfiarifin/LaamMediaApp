package com.laam.laamarticle.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.laam.laamarticle.R
import com.laam.laamarticle.models.response.ResponseLogin
import com.laam.laamarticle.services.SharedPrefHelper
import com.laam.laamarticle.services.api.ServiceBuilder
import com.laam.laamarticle.services.api.UserService
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login_tv_register.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        }

        login_btn_login.setOnClickListener {
            onLoginPressed()
        }
    }

    private fun onLoginPressed() {
        ServiceBuilder.buildService(UserService::class.java)
            .postLogin(login_et_email.text.toString(), login_et_password.text.toString()).enqueue(
                object : Callback<ResponseLogin> {
                    override fun onResponse(
                        call: Call<ResponseLogin>,
                        response: Response<ResponseLogin>
                    ) {
                        Toast.makeText(
                            this@LoginActivity,
                            response.body()!!.message,
                            Toast.LENGTH_SHORT
                        ).show()
                        if (response.body()!!.success) {
                            val pref = SharedPrefHelper(this@LoginActivity)
                            pref.saveUser(
                                response.body()!!.user!!
                            )
                            Log.d("laam", "id : ${response.body()!!.user!!.id}" )
                            startActivity(
                                Intent(
                                    this@LoginActivity,
                                    MainActivity::class.java
                                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            )
                        }
                    }

                    override fun onFailure(call: Call<ResponseLogin>, t: Throwable) {
                        Toast.makeText(
                            this@LoginActivity,
                            "Error : ${t.message}",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        Log.e("onFailure", t.message)
                    }
                })
    }
}
