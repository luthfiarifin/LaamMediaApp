package com.laam.laammedia.models


import com.google.gson.annotations.SerializedName

data class Message(
    @SerializedName("like_count")
    val likeCount: Int
)