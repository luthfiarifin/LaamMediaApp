package com.laam.laammedia.services.api

import com.laam.laammedia.models.Category
import com.laam.laammedia.models.response.ResponseDB
import com.laam.laammedia.models.User
import com.laam.laammedia.models.response.ResponseLogin
import retrofit2.Call
import retrofit2.http.*

interface UserService {

    @GET("user/byid")
    fun getUserByID(
        @Query("user_id") user_id: Int,
        @Query("post_user_id") post_user_id: Int
    ): Call<User>

    @FormUrlEncoded
    @POST("user/following")
    fun postFollowing(
        @Field("user_id") user_id: Int,
        @Field("following_id") following_id: Int
    ): Call<ResponseDB>

    @DELETE("user/following/{user_id}/{following_id}")
    fun deleteUnfollowing(
        @Path("user_id") user_id: Int,
        @Path("following_id") following_id: Int
    ): Call<ResponseDB>

    @GET("user/job")
    fun getJobCategory(): Call<List<Category>>

    @FormUrlEncoded
    @POST("user/login")
    fun postLogin(
        @Field("email") email: String,
        @Field("pass") password: String
    ): Call<ResponseLogin>

    @FormUrlEncoded
    @POST("user/register")
    fun postRegister(
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("job_id") job_id: Int,
        @Field("name") name: String,
        @Field("bio") bio: String,
        @Field("image_url") image_url: String
    ): Call<ResponseDB>

    @FormUrlEncoded
    @PUT("user/edit")
    fun putProfile(
        @Field("id") id: Int,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("job_id") job_id: Int,
        @Field("name") name: String,
        @Field("bio") bio: String,
        @Field("image_url") image_url: String? = null
    ): Call<ResponseDB>
}