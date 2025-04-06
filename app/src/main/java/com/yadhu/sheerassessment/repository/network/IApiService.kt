package com.yadhu.sheerassessment.repository.network

import com.yadhu.sheerassessment.model.user.User
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API interface for defining all the APIs and its semantics.
 */
interface IApiService {

    @GET("users/{username}")
    suspend fun getUser(@Path("username") username: String): Response<User>

    @GET("users/{username}/followers")
    suspend fun getFollowers(@Path("username") username: String,
                             @Query("per_page") perPage: Int = 50,
                             @Query("page") page: Int): Response<List<User>>

    @GET("users/{username}/following")
    suspend fun getFollowing(@Path("username") username: String,
                             @Query("per_page") perPage: Int = 50,
                             @Query("page") page: Int): Response<List<User>>
}