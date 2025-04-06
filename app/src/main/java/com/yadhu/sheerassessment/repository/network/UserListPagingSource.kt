package com.yadhu.sheerassessment.repository.network

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.yadhu.sheerassessment.model.user.User

private const val TAG = "UserListPagingSource"

class UserListPagingSource(private val networkRepository: INetworkRepository,
                           private val username: String,
                           private val isFollower: Boolean): PagingSource<Int, User>() {
    override fun getRefreshKey(state: PagingState<Int, User>): Int? {
        return state.anchorPosition?.let {
            state.closestPageToPosition(it)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(it)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, User> {
        try {
            val page = params.key ?: 1
            val response = if (isFollower) {
                networkRepository.getFollowers(username, page)
            } else {
                networkRepository.getFollowing(username, page)
            }

            if (response is APIResponse.Success) {
                return LoadResult.Page(data = response.data,
                    prevKey = if (page == 1) null else page - 1,
                    nextKey = if (response.data.isEmpty()) null else page + 1)
            }

            return LoadResult.Error(IllegalStateException("No response"))
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }
}