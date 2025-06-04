package com.francotte

import com.francotte.authenticate
import com.francotte.data.FavoriteDataSource
import com.francotte.data.UserDataSource
import com.francotte.security.hashing.HashingInterface
import com.francotte.security.token.TokenConfig
import com.francotte.security.token.TokenInterface
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers

fun Route.addFavoriteRecipe(userDataSource: FavoriteDataSource, ) {
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

