package com.laam.laammedia.models.response

import com.google.gson.annotations.SerializedName
import com.laam.laammedia.models.User

data class ResponseLogin(
    val success: Boolean,
    val message: String,
    @SerializedName("data")
    val user: User? = null
)