package com.laam.laammedia.services.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServiceBuilder {

    const val BASE_URL = "http://10.0.2.2:3003"
    const val URL = "$BASE_URL/"

    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(BasicAuthInterceptor("laam", "laam123"))
        .build()

    private val builder = Retrofit.Builder()
        .baseUrl(URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttp)

    private val retrofit = builder.build()

    fun <T> buildService(serviceType: Class<T>): T {
        return retrofit.create(serviceType)
    }
}