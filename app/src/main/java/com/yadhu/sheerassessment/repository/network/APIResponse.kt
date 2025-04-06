package com.yadhu.sheerassessment.repository.network

sealed class APIResponse<out T> {
    data class Success<T>(val data: T) : APIResponse<T>()
    data class Failure(val errorCode: Int, val errorMessage: String) : APIResponse<Nothing>()
}