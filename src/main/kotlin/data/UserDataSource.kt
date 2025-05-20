package com.francotte.data

import com.francotte.data.models.User

interface UserDataSource {
    suspend fun getUserByName(userName: String):User?
    suspend fun insertUser(user:User) : Boolean
}