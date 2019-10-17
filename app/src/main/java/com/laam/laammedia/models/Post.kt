package com.laam.laammedia.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Post(
    val id: Int,
    val title: String,
    val content: String,
    @SerializedName("image_url")
    val imageUrl: String,
    @SerializedName("author_id")
    val authorID: Int,
    @SerializedName("author_name")
    val authorName: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("like_count")
    val likeCount: Int,
    val liked: Int = 0
) : Parcelable