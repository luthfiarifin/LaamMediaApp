package com.laam.laammedia.models


import com.google.gson.annotations.SerializedName

data class CommentCount(
    @SerializedName("comment_count")
    val commentCount: Int
)