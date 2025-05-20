package com.francotte.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(val token:String)