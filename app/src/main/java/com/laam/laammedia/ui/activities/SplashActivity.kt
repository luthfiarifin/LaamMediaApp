package com.laam.laammedia.ui.activities

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import com.laam.laammedia.R
import com.laam.laammedia.models.User
import com.laam.laammedia.services.SharedPrefHelper
import com.laam.laammedia.services.api.ServiceBuilder
import com.laam.laammedia.services.api.UserService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val pref = SharedPrefHelper(this@SplashActivity)

        Handler().postDelayed({
            DoAsync(
                { if (pref.isLoggedIn()) { saveUser() } },
                {
                    if (pref.isLoggedIn()) {
                        isLogin()
                    } else {
                        isNotLogin()
                    }
                }
            ).execute()
        }, 2000)
    }

    private fun isLogin() {
        startActivity(
            Intent(
                this@SplashActivity,
                MainActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }

    private fun isNotLogin() {
        startActivity(
            Intent(
                this@SplashActivity,
                LoginActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }

    private fun saveUser() {
        val pref = SharedPrefHelper(this@SplashActivity)
        ServiceBuilder.buildService(UserService::class.java)
            .getUserByID(0, pref.getAccount().id).enqueue(
                object : Callback<User> {
                    override fun onResponse(
                        call: Call<User>,
                        response: Response<User>
                    ) {
                        pref.saveUser(
                            response.body()!!
                        )
                    }

                    override fun onFailure(call: Call<User>, t: Throwable) {
                        Toast.makeText(
                            this@SplashActivity,
                            "Error : ${t.message}",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        Log.e("onFailure", t.message)
                    }
                })
    }

    private class DoAsync(val onBackground: () -> Unit, val onPost: () -> Unit) :
        AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg p0: Void?): Void? {
            onBackground()
            return null
        }

        override fun onPostExecute(result: Void?) {
            onPost()
        }
    }
}
