package com.yadhu.sheerassessment.ui.search

import android.util.Log
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "SearchViewModel"

sealed interface SearchUIState {
    data class HasData(val isLoading: Boolean, val data: User) : SearchUIState
    data class NoData(
        val isLoading: Boolean,
        val code: Int?,
        val message: String?
    ) : SearchUIState
}

data class SearchViewModelState(
    val isLoading: Boolean = false,
    val code: Int? = null,
    val message: String? = null,
    val data: User? = null
) {

    fun toUIState(): SearchUIState {
        return if (data != null) {
            SearchUIState.HasData(isLoading = isLoading, data = data)
        } else {
            SearchUIState.NoData(isLoading = isLoading, code = code, message = message)
        }
    }
}

class SearchViewModel(private val networkRepository: INetworkRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    private val viewModelState = MutableStateFlow(
        SearchViewModelState(
            isLoading = false,
            code = null,
            message = null,
            data = null
        )
    )

    val uiState: StateFlow<SearchUIState> = viewModelState
        .map { it.toUIState() }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            viewModelState.value.toUIState()
        )

    init {
        setupSearchQueryObserver()
    }

    fun updateSearchQuery(query: String?) {
        query?.let {
            Log.d(TAG, "Query = $it")
            _searchQuery.value = it
        }
    }

    private fun setupSearchQueryObserver() {
        viewModelScope.launch {
            _searchQuery
                .collectLatest {
                    if (it.isEmpty()) {
                        viewModelState.update { state ->
                            state.copy(isLoading = false, data = null)
                        }
                    } else {
                        getUserFromServer()
                    }
                }
        }
    }

    private fun getUserFromServer() {
        viewModelState.update {
            it.copy(isLoading = true)
        }

        viewModelScope.launch(Dispatchers.IO) {
            val response = networkRepository.getUser(_searchQuery.value)
            if (response is APIResponse.Success) {
                viewModelState.update {
                    it.copy(isLoading = false, data = response.data)
                }
            } else if (response is APIResponse.Failure) {
                viewModelState.update {
                    it.copy(
                        isLoading = false,
                        code = response.errorCode,
                        message = response.errorMessage
                    )
                }
            }
        }
    }

    companion object {
        fun getSearchViewModelFactory(networkRepository: INetworkRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
                        return SearchViewModel(networkRepository) as T
                    }
                    throw IllegalArgumentException("Wrong argument passed for creating ViewModel")
                }
            }
        }
    }
}