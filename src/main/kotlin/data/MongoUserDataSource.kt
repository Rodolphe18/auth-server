package com.francotte.data

import com.francotte.data.models.User
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class MongoUserDataSource(db:CoroutineDatabase) : UserDataSource {

    private val users = db.getCollection<User>()

    override suspend fun getUserByName(userName: String): User? {
        return users.findOne(User::username eq userName)
    }

    override suspend fun insertUser(user: User): Boolean {
        return users.insertOne(user).wasAcknowledged()
    }

    override suspend fun getUsers():CoroutineCollection<User> = users

}