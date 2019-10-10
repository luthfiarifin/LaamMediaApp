package com.laam.laamarticle.models.response

import com.laam.laamarticle.models.Message

data class ResponseLikePost(
    val success: Boolean,
    val message: Message
)