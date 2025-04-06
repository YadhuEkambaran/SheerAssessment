package com.yadhu.sheerassessment.ui.userlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yadhu.sheerassessment.model.user.User
import com.yadhu.sheerassessment.repository.network.APIResponse
import com.yadhu.sheerassessment.repository.network.INetworkRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface UserListUIState {
    data class HasData(val isLoading: Boolean, val data: List<User>): UserListUIState
    data class NoData(val isLoading: Boolean, val message: String?): UserListUIState
}

private data class UserListViewModelState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val data: List<User>? = null
) {
    fun toUIState(): UserListUIState {
        return if (data != null) {
            UserListUIState.HasData(isLoading = isLoading, data = data)
        } else {
            UserListUIState.NoData(isLoading = isLoading, message = message)
        }
    }
}


class UserListViewModel(private val networkRepository: INetworkRepository): ViewModel() {

    private val _username = MutableStateFlow("")
    private val _isFollower = MutableStateFlow(false)


    private val _viewModelState = MutableStateFlow(UserListViewModelState())

    val uiState: StateFlow<UserListUIState> = _viewModelState
        .map { it.toUIState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, _viewModelState.value.toUIState())

    init {
        usernameChangeObserver()
    }

    fun updateUserName(pair: Pair<String, Boolean>) {
        _username.value = pair.first
        _isFollower.value = pair.second
    }

    private fun usernameChangeObserver() {
        viewModelScope.launch {
            _username.collectLatest {
                if (_isFollower.value) {
                    getUserList(networkRepository::getFollowers)
                } else {
                    getUserList(networkRepository::getFollowing)
                }
            }
        }
    }

    private fun getUserList(apiCall: suspend (String) -> APIResponse<List<User>>) {
        _viewModelState.update {
            it.copy(isLoading = true)
        }

        viewModelScope.launch(Dispatchers.IO) {
            when (val response = apiCall(_username.value)) {
                is APIResponse.Success -> {
                    _viewModelState.update {
                        it.copy(isLoading = false, data = response.data)
                    }
                }

                is APIResponse.Failure -> {
                    _viewModelState.update {
                        it.copy(isLoading = false, message = response.errorMessage)
                    }
                }
            }
        }
    }

    companion object {
        fun getUserListViewModelFactory(networkRepository: INetworkRepository) : ViewModelProvider.Factory {
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