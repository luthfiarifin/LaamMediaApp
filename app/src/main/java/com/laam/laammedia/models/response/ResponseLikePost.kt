package com.laam.laammedia.models.response

import com.laam.laammedia.models.Message

data class ResponseLikePost(
    val success: Boolean,
    val message: Message
)