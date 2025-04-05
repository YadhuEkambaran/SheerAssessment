package com.yadhu.sheerassessment.repository.network

import com.yadhu.sheerassessment.model.user.User

interface INetworkRepository {
    suspend fun getUser(username: String): APIResponse<User>
    suspend fun getFollowers(username: String): APIResponse<List<User>>
    suspend fun getFollowing(username: String): APIResponse<List<User>>
}