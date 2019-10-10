package com.laam.laamarticle.models


import com.google.gson.annotations.SerializedName

data class DirrectMessage(
    val id: Int,
    @SerializedName("destination_id")
    val destinationID: Int,
    val content: String,
    @SerializedName("image_url")
    val imageUrl: String,
    @SerializedName("created_at")
    val createdAt: String
)