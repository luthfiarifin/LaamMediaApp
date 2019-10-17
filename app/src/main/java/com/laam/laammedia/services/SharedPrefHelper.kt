package com.laam.laammedia.services

import android.content.Context
import android.content.SharedPreferences
import com.laam.laammedia.models.User

class SharedPrefHelper(val context: Context) {
    private val PREF_NAME = "laampref"
    private val ID_KEY = "id_key"
    private val EMAIL_KEY = "email_key"
    private val PASS_KEY = "pass_key"
    private val NAME_KEY = "name_key"
    private val JOB_KEY = "job_key"
    private val ID_JOB_KEY = "id_job_key"
    private val BIO_KEY = "bio_key"
    private val IMAGE_KEY = "image_key"
    private val C_POST_KEY = "c_post_key"
    private val C_FLWR_KEY = "c_flwr_key"
    private val C_FLWI_KEY = "c_flwi_key"
    private val FLWI_KEY = "flwi_key"

    val sharedPref: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveUser(user: User) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putInt(ID_KEY, user.id)
        editor.putString(EMAIL_KEY, user.email)
        editor.putString(PASS_KEY, user.password)
        editor.putString(NAME_KEY, user.name)
        editor.putString(JOB_KEY, user.jobCategory)
        editor.putInt(ID_JOB_KEY, user.jobId)
        editor.putString(BIO_KEY, user.bio)
        editor.putString(IMAGE_KEY, user.imageUrl)
        editor.putString(C_POST_KEY, user.postCount)
        editor.putString(C_FLWR_KEY, user.followerCount)
        editor.putString(C_FLWI_KEY, user.followingCount)
        editor.putInt(FLWI_KEY, user.following)
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        return sharedPref.getInt(ID_KEY, 0) != 0
    }

    fun getAccount(): User {
        return User(
            sharedPref.getInt(ID_KEY, 0),
            sharedPref.getString(EMAIL_KEY, null)!!,
            sharedPref.getString(PASS_KEY, null)!!,
            sharedPref.getString(NAME_KEY, null)!!,
            sharedPref.getString(JOB_KEY, null)!!,
            sharedPref.getInt(ID_JOB_KEY, 0),
            sharedPref.getString(BIO_KEY, null)!!,
            sharedPref.getString(IMAGE_KEY, null)!!,
            sharedPref.getString(C_POST_KEY, null)!!,
            sharedPref.getString(C_FLWR_KEY, null)!!,
            sharedPref.getString(C_FLWI_KEY, null)!!,
            sharedPref.getInt(FLWI_KEY, 0)
        )
    }

    fun clearUser() {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.clear()
        editor.apply()
    }
}