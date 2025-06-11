package com.francotte

import com.francotte.data.FavoriteDataSource
import com.francotte.data.UserDataSource
import com.francotte.security.hashing.HashingInterface
import com.francotte.security.token.TokenConfig
import com.francotte.security.token.TokenInterface
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting(favoriteDataSource: FavoriteDataSource,userDataSource: UserDataSource, hashingInterface: HashingInterface, tokenInterface: TokenInterface, tokenConfig: TokenConfig, token:String) {
    routing {
        signUp(hashingInterface, userDataSource,tokenInterface,tokenConfig)
        signIn(hashingInterface,userDataSource)
        deleteUser(userDataSource)
        authenticate()
        getSecretInfo()
        addFavoriteRecipe(favoriteDataSource)
        deleteFavoriteRecipe(favoriteDataSource)
        getFavoriteRecipes(favoriteDataSource)
        isRecipeInFavorites(favoriteDataSource)
    }
}
