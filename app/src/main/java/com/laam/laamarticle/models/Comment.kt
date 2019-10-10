package com.laam.laamarticle.models


import com.google.gson.annotations.SerializedName

data class Comment(
    val id: Int,
    val name: String,
    @SerializedName("user_id")
    val userID: Int,
    @SerializedName("image_url")
    val imageUrl: String,
    val content: String,
    @SerializedName("created_at")
    val createdAt: String
)