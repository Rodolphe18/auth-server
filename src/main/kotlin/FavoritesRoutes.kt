package com.francotte


import com.francotte.data.FavoriteDataSource
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import java.io.File
import java.util.*

fun Route.addFavoriteRecipe(userDataSource: FavoriteDataSource) {
    authenticate {
        post("users/favorites/{recipeId}") {
            with(Dispatchers.IO) {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.getClaim("userId", String::class)?.toLongOrNull()

                val recipeId = call.parameters["recipeId"]
                if (userId == null || recipeId.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, "Paramètres invalides.")
                    return@post
                }

                val success = userDataSource.addFavorite(userId, recipeId)
                if (success) {
                    call.respond(HttpStatusCode.OK, "Ajouté aux favoris.")
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Erreur lors de l'ajout.")
                }
            }
        }
    }
}


fun Route.deleteFavoriteRecipe(favoriteDataSource: FavoriteDataSource) {
    authenticate {
        delete("users/favorites/{recipeId}") {
            with(Dispatchers.IO) {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.getClaim("userId", String::class)?.toLongOrNull()

                val recipeId = call.parameters["recipeId"]
                if (userId == null || recipeId.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, "Paramètres invalides.")
                    return@delete
                }

                val success = favoriteDataSource.removeFavorite(userId, recipeId)
                if (success) {
                    call.respond(HttpStatusCode.OK, "Retiré des favoris.")
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Erreur lors de la suppression.")
                }
            }
        }
    }
}

fun Route.getFavoriteRecipes(favoriteDataSource: FavoriteDataSource) {
    authenticate {
        get("users/favorites") {
            with(Dispatchers.IO) {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.getClaim("userId", String::class)?.toLongOrNull()

                if (userId == null) {
                    call.respond(HttpStatusCode.BadRequest, "Token invalide ou manquant.")
                    return@get
                }

                val favorites = favoriteDataSource.getFavorites(userId)
                if (favorites != null) {
                    call.respond(HttpStatusCode.OK, favorites)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Aucun favori trouvé.")
                }
            }
        }
    }
}


fun Route.isRecipeInFavorites(favoriteDataSource: FavoriteDataSource) {
    authenticate {
        get("users/favorites/{recipeId}/status") {
            with(Dispatchers.IO) {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.getClaim("userId", String::class)?.toLongOrNull()
                val recipeId = call.parameters["recipeId"]

                if (userId == null || recipeId.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, "Paramètres invalides.")
                    return@get
                }

                val isFavorite = favoriteDataSource.isFavorite(userId, recipeId)
                call.respond(HttpStatusCode.OK, isFavorite)
            }
        }
    }
}

fun Route.addRecipe(favoriteDataSource: FavoriteDataSource) {
    authenticate {
        post("/users/recipes") {
            val multipart = call.receiveMultipart()
            var title: String? = null
            var instructions: String? = null
            var ingredientsJson: String? = null
            val images = mutableListOf<PartData.FileItem>()

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        when (part.name) {
                            "title" -> title = part.value
                            "instructions" -> instructions = part.value
                            "ingredients" -> ingredientsJson = part.value
                        }
                    }

                    is PartData.FileItem -> {
                        if (part.name == "images") {
                            images.add(part)
                        }
                    }

                    else -> Unit
                }
                part.dispose()
            }

            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)?.toLongOrNull()

            if (title == null || instructions == null || ingredientsJson == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing fields")
                return@post
            }
            if (userId == null) {
                call.respond(HttpStatusCode.BadRequest, "User id invalide")
                return@post
            }

            val ingredients: List<Pair<String, String>> = try {
                Json.decodeFromString(ingredientsJson!!)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ingredient format")
                return@post
            }

            // Enregistrer les fichiers images dans un dossier (ou cloud), récupérer leurs URLs
            val imageUrls = images.map { file ->
                val name = "uploads/${UUID.randomUUID()}.jpg"
                val fileBytes = file.streamProvider().readBytes()
                File(name).writeBytes(fileBytes)
                name // ou l’URL si upload sur S3/Cloudinary
            }

            val success = favoriteDataSource.addRecipe(
                userId = userId,
                recipeTitle = title!!,
                instructions = instructions!!,
                ingredients = ingredients,
                imageUrls = imageUrls
            )

            if (success) call.respond(HttpStatusCode.Created)
            else call.respond(HttpStatusCode.InternalServerError)
        }
    }
}