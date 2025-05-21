package com.francotte.data.requests

import com.francotte.data.models.User
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(val token:String, val user: User)