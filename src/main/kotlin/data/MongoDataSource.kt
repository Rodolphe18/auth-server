package com.francotte.data

import com.francotte.data.models.User
import com.francotte.data.models.UserRecipe
import org.litote.kmongo.addToSet
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.pull
import org.litote.kmongo.push

class MongoDataSource(db:CoroutineDatabase) : UserDataSource, FavoriteDataSource {

    private val users = db.getCollection<User>()

    override suspend fun getUserByName(userName: String): User? {
        return users.findOne(User::username eq userName)
    }

    override suspend fun insertUser(user: User): Boolean {
        return users.insertOne(user).wasAcknowledged()
    }

    override suspend fun getUsers() = users

    override suspend fun deleteUserById(userId: Long): Boolean {
        val result = users.deleteOne(User::userId eq userId)
        return result.wasAcknowledged() && result.deletedCount > 0
    }

    override suspend fun addFavorite(userId: Long, recipeId: String): Boolean {
        val updateResult = users.updateOne(
            User::userId eq userId,
            addToSet(User::favoriteIds, recipeId)
        )
        return updateResult.wasAcknowledged()
    }

    override suspend fun removeFavorite(userId: Long, recipeId: String): Boolean {
        val updateResult = users.updateOne(
            User::userId eq userId,
            pull(User::favoriteIds, recipeId)
        )
        return updateResult.wasAcknowledged()
    }

    override suspend fun getFavorites(userId: Long): List<String>? {
        return users.findOne(User::userId eq userId)?.favoriteIds
    }

    override suspend fun isFavorite(userId: Long, recipeId: String): Boolean {
        val user = users.findOne(User::userId eq userId)
        return user?.favoriteIds?.contains(recipeId) == true
    }

    override suspend fun addRecipe(
        userId: Long,
        recipeTitle: String,
        instructions: String,
        ingredients: List<Pair<String, String>>,
        imageUrls: List<String>
    ): Boolean {
        val recipe = UserRecipe(
            title = recipeTitle,
            instructions = instructions,
            ingredients = ingredients,
            imageUrls = imageUrls
        )

        val update = users.updateOne(
            User::userId eq userId,
            push(User::userRecipes, recipe)
        )

        return update.wasAcknowledged()
    }

}

