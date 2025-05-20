package com.francotte

import com.francotte.authenticate
import com.francotte.data.UserDataSource
import com.francotte.data.models.User
import com.francotte.data.requests.AuthRequest
import com.francotte.data.requests.AuthResponse
import com.francotte.security.hashing.HashingInterface
import com.francotte.security.hashing.SaltedHash
import com.francotte.security.token.TokenClaim
import com.francotte.security.token.TokenConfig
import com.francotte.security.token.TokenInterface
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.signUp(hashingInterface: HashingInterface, userDataSource: UserDataSource) {

    post("signup") {
        val request = runCatching { call.receiveNullable<AuthRequest>() }.getOrElse {
            call.respond(status = HttpStatusCode.BadRequest, message = "")
            return@post
        }
        request?.let { req ->
            val areFieldsBlank = req.username.isBlank() || req.password.isBlank()
            val isPwdTooShort = req.password.length < 10
            if (areFieldsBlank || isPwdTooShort) {
                call.respond(status = HttpStatusCode.Conflict, "")
            }
            val saltedHash = hashingInterface.generateSaltedHash(req.password)
            val user = User(username = req.username, password = saltedHash.hash, salt = saltedHash.salt)
            val wasAcknowledged = userDataSource.insertUSer(user)
            if (!wasAcknowledged) {
                call.respond(status = HttpStatusCode.Conflict, "")
                return@post
            }
            call.respond(status =HttpStatusCode.OK, "")
        }
    }
}

fun Route.signIn(hashingInterface: HashingInterface, userDataSource: UserDataSource, tokenInterface: TokenInterface, tokenConfig: TokenConfig) {

    post("signin") {
        val request = runCatching { call.receiveNullable<AuthRequest>() }.getOrElse {
            call.respond(status = HttpStatusCode.BadRequest, message = "")
            return@post
        }

        request?.let { req ->
            val user = userDataSource.getUserByName(req.username)
            if (user == null) {
                call.respond(status =HttpStatusCode.Conflict, "")
                return@post
            }
            val isValidPassword = hashingInterface.verifyHash(value = req.password, saltedHash = SaltedHash(hash = user.password, salt = user.salt))
            if (!isValidPassword) {
                call.respond(status =HttpStatusCode.Conflict, "")
                return@post
            }
            val token = tokenInterface.generate(config = tokenConfig, TokenClaim(name = "userId", value = user.id.toString()))
            call.respond(status =HttpStatusCode.OK, message = AuthResponse(token = token))
        }
    }
}

fun Route.authenticate() {
    authenticate {
        get("authenticate") {
            call.respond(HttpStatusCode.OK)
        }
    }
}

fun Route.getSecretInfo() {
    authenticate {
        get("secret") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)
            call.respond(HttpStatusCode.OK, "your userId is $userId")
        }
    }
}