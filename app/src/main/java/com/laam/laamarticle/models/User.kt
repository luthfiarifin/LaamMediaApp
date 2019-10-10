package com.laam.laamarticle.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    val id: Int,
    val email: String,
    val password: String,
    val name: String,
    @SerializedName("job_category")
    val jobCategory: String,
    val bio: String,
    @SerializedName("image_url")
    val imageUrl: String,
    @SerializedName("post_count")
    val postCount: String,
    @SerializedName("follower_count")
    val followerCount: String,
    @SerializedName("following_count")
    val followingCount: String,
    @SerializedName("following")
    val following: Int
) : Parcelable