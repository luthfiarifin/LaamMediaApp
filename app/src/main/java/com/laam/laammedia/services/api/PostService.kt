package com.laam.laammedia.services.api

import com.laam.laammedia.models.*
import com.laam.laammedia.models.response.ResponseDB
import com.laam.laammedia.models.response.ResponseLikePost
import retrofit2.Call
import retrofit2.http.*

interface PostService {

    @GET("post/cat")
    fun getPostDiscover(
        @Query("user_id") user_id: Int,
        @Query("cat_id") cat_id: Int,
        @Query("title") title: String
    ): Call<List<Post>>

    @GET("post/following")
    fun getPostHome(
        @Query("id") user_id: Int
    ): Call<List<Post>>

    @GET("post/profile")
    fun getPostProfile(
        @Query("id") user_id: Int
    ): Call<List<Post>>

    @GET("post/byid")
    fun getPostByID(
        @Query("user_id") user_id: Int,
        @Query("post_id") post_id: Int
    ): Call<List<Post>>

    @FormUrlEncoded
    @POST("post/like")
    fun postLike(
        @Field("user_id") user_id: Int,
        @Field("post_id") post_id: Int
    ): Call<ResponseLikePost>

    @DELETE("post/like/{user_id}/{post_id}")
    fun deleteLike(
        @Path("user_id") user_id: Int,
        @Path("post_id") post_id: Int
    ): Call<ResponseLikePost>

    @GET("category")
    fun getCategory(): Call<List<Category>>

    @GET("post/headermessage")
    fun getHeaderMessage(
        @Query("id") user_id: Int
    ): Call<List<HeaderMessage>>

    @GET("post/listfollowing")
    fun getListFollowing(
        @Query("user_id") user_id: Int,
        @Query("search") search: String = ""
    ): Call<List<HeaderMessage>>

    @FormUrlEncoded
    @POST("post/add")
    fun addPost(
        @Field("user_id") user_id: Int,
        @Field("category_id") category_id: Int,
        @Field("title") title: String,
        @Field("content") content: String,
        @Field("image") image: String
    ): Call<ResponseDB>
}