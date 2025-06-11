package com.francotte.data.models

import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class UserRecipe(
    val id: String = ObjectId().toString(),
    val title: String,
    val instructions: String,
    val ingredients: List<Pair<String, String>>,
    val imageUrls: List<String>
)