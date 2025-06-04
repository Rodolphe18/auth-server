package com.francotte.data


interface FavoriteDataSource {
    suspend fun addFavorite(userId: Long, recipeId: String): Boolean
    suspend fun removeFavorite(userId: Long, recipeId: String): Boolean
    suspend fun getFavorites(userId: Long): List<String>?
}