package com.laam.laamarticle.services.api

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class BasicAuthInterceptor(var user: String, var password: String) : Interceptor {
    private var credentials: String = ""

    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        val authReq = req.newBuilder()
            .header("Authorization", Credentials.basic(user, password)).build()
        return chain.proceed(authReq)
    }
}