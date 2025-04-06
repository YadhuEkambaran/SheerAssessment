package com.yadhu.sheerassessment.repository.network

/**
 * This class holds the response from the API calls.
 * This is a generic method which can be used with all the API calls.
 */
sealed class APIResponse<out T> {
    /**
     * It is for return the response to the Intermediate layer when the API call is success
     */
    data class Success<T>(val data: T) : APIResponse<T>()

    /**
     * This is used when the API call was failure
     * [errorCode] and [errorMessage] are sent to the upper layer and it can decide
     * actions in response to this codes.
     */
    data class Failure(val errorCode: Int, val errorMessage: String) : APIResponse<Nothing>()
}