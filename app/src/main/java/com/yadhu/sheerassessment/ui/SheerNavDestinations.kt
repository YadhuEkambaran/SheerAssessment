package com.yadhu.sheerassessment.ui

import kotlinx.serialization.Serializable

@Serializable
data class Search(val username: String?)

@Serializable
data class UserList(val username: String?, val isFollower: Boolean)