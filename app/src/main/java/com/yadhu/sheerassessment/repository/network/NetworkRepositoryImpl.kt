package com.yadhu.sheerassessment.repository.network

import android.util.Log
import com.yadhu.sheerassessment.model.user.User
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val LOCAL_TRANSFORMATION_EXCEPTION = 1002

private const val TAG = "NetworkRepositoryImpl"

class NetworkRepositoryImpl: INetworkRepository {

    private val apiService: IApiService

    init {
        val okHttpClient = OkHttpClient.Builder().addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer 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")
                .build()
            chain.proceed(request)
        }.build()

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(IApiService::class.java)
    }

    override suspend fun getUser(username: String): APIResponse<User> {
        Log.d(TAG, "getUser")
        return transform(apiService.getUser(username))
    }

    override suspend fun getFollowers(username: String): APIResponse<List<User>> {
        return transform(apiService.getFollowers(username))
    }

    override suspend fun getFollowing(username: String): APIResponse<List<User>> {
        return transform(apiService.getFollowing(username))
    }

    private fun <T> transform(response: Response<T>): APIResponse<T> {
        try {
            if (response.isSuccessful && response.body() != null) {
                return APIResponse.Success(response.body()!!)
            }
            return APIResponse.Failure(response.code(), response.message())
        } catch (ex: Exception) {
            return APIResponse.Failure(LOCAL_TRANSFORMATION_EXCEPTION, "Error while Transformation")
        }
    }

    companion object {
        private var INSTANCE: INetworkRepository? = null

        fun getNetworkInstance(): INetworkRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = NetworkRepositoryImpl()
                INSTANCE = instance
                INSTANCE!!
            }
        }
    }
}

