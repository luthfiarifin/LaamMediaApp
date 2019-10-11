package com.laam.laamarticle.models.response

import com.google.gson.annotations.SerializedName
import com.laam.laamarticle.models.User

data class ResponseLogin(
    val success: Boolean,
    val message: String,
    @SerializedName("data")
    val user: User? = null
)