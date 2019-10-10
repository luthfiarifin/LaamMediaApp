package com.laam.laamarticle.models

import com.google.gson.annotations.SerializedName

data class HeaderMessage(
    val id: Int,
    val userName: String,
    @SerializedName("destination_id")
    val destinationId: Int,
    @SerializedName("destination_name")
    val destinationName: String,
    @SerializedName("image_url")
    val imageUrl: String,
    val content: String? = null,
    @SerializedName("created_at")
    val createdAt: String
)