package com.francotte

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.francotte.data.UserDataSource
import com.francotte.security.hashing.HashingInterface
import com.francotte.security.token.TokenConfig
import com.francotte.security.token.TokenInterface
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.event.*

fun Application.configureRouting(userDataSource: UserDataSource, hashingInterface: HashingInterface, tokenInterface: TokenInterface, tokenConfig: TokenConfig, token:String) {
    routing {
        signUp(hashingInterface, userDataSource,tokenInterface,tokenConfig)
        signIn(hashingInterface,userDataSource,token)
        authenticate()
        getSecretInfo()
    }
}
