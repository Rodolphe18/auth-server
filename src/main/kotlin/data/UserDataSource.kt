package com.francotte.data

import com.francotte.data.models.User
import org.litote.kmongo.coroutine.CoroutineCollection

interface UserDataSource {
    suspend fun getUserByName(userName: String):User?
    suspend fun insertUser(user:User) : Boolean
    suspend fun getUsers(): CoroutineCollection<User>
}