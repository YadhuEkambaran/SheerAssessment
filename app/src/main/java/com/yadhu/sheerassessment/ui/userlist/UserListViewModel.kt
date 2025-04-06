package com.yadhu.sheerassessment.ui.userlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.yadhu.sheerassessment.model.user.User
import com.yadhu.sheerassessment.repository.network.INetworkRepository
import com.yadhu.sheerassessment.repository.network.UserListPagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest

private const val TAG = "UserListViewModel"

class UserListViewModel(private val networkRepository: INetworkRepository) : ViewModel() {

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _isFollower = MutableStateFlow(false)

    val users: Flow<PagingData<User>> = _username
        .flatMapLatest { username ->
            Pager(config = PagingConfig(pageSize = 20),
                pagingSourceFactory = {
                    UserListPagingSource(
                        networkRepository,
                        username,
                        _isFollower.value
                    )
                }).flow
        }
        .cachedIn(viewModelScope)

    fun updateUserName(pair: Pair<String, Boolean>) {
        _username.value = pair.first
        _isFollower.value = pair.second
    }

    companion object {
        fun getUserListViewModelFactory(networkRepository: INetworkRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(UserListViewModel::class.java)) {
                        return UserListViewModel(networkRepository) as T
                    }

                    throw IllegalArgumentException("Wrong argument passed to viewmodel")
                }
            }
        }
    }

}