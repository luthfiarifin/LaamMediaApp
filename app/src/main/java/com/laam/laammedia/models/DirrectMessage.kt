package com.laam.laammedia.models


import com.google.gson.annotations.SerializedName

data class DirrectMessage(
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    val content: String,
    @SerializedName("image_url")
    val imageUrl: String,
    @SerializedName("created_at")
    val createdAt: String
)