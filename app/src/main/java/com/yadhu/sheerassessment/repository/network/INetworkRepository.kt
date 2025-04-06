package com.yadhu.sheerassessment.repository.network

import com.yadhu.sheerassessment.model.user.User

/**
 * [INetworkRepository] interact with the UI and [NetworkRepositoryImpl]
 * This provides the functions for calling the APIs.
 */
interface INetworkRepository {
    suspend fun getUser(username: String): APIResponse<User>
    suspend fun getFollowers(username: String, page: Int): APIResponse<List<User>>
    suspend fun getFollowing(username: String, page: Int): APIResponse<List<User>>
}