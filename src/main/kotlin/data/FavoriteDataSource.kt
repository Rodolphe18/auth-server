package com.francotte.data

import com.sun.jndi.toolkit.url.Uri


interface FavoriteDataSource {
    suspend fun addFavorite(userId: Long, recipeId: String): Boolean
    suspend fun removeFavorite(userId: Long, recipeId: String): Boolean
    suspend fun getFavorites(userId: Long): List<String>?
    suspend fun isFavorite(userId: Long, recipeId: String): Boolean
    suspend fun addRecipe(userId: Long,
                          recipeTitle: String,
                          instructions: String,
                          ingredients: List<Pair<String, String>>,
                          imageUrls: List<String>): Boolean
}