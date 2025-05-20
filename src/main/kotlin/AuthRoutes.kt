package com.francotte

import com.francotte.data.UserDataSource
import com.francotte.data.models.User
import com.francotte.data.requests.AuthRequest
import com.francotte.data.requests.AuthResponse
import com.francotte.security.hashing.Hash
import com.francotte.security.hashing.HashingInterface
import com.francotte.security.token.TokenClaim
import com.francotte.security.token.TokenConfig
import com.francotte.security.token.TokenInterface
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers

fun Route.signUp(hashingInterface: HashingInterface, userDataSource: UserDataSource) {
    post("signup") {
        with(Dispatchers.IO) {
            val request = runCatching { call.receive<AuthRequest>() }.getOrElse {
                call.respond(status = HttpStatusCode.BadRequest, message = "")
                return@post
            }

            val areFieldsBlank = request.username.isBlank() || request.password.isBlank()
            val isPwdTooShort = request.password.length < 5
            if (areFieldsBlank || isPwdTooShort) {
                call.respond(status = HttpStatusCode.Gone, "")
            }
            val hash = hashingInterface.generateHash(request.password)
            val user = User(username = request.username, password = hash.hash)
            val wasAcknowledged = userDataSource.insertUser(user)
            if (!wasAcknowledged) {
                call.respond(status = HttpStatusCode.Conflict, "")
                return@post
            }
            call.respond(status = HttpStatusCode.OK, "")
        }

    }
}

fun Route.signIn(
    hashingInterface: HashingInterface,
    userDataSource: UserDataSource,
    tokenInterface: TokenInterface,
    tokenConfig: TokenConfig
) {

    post("signin") {
        with(Dispatchers.IO) {
            val request = runCatching { call.receive<AuthRequest>() }.getOrElse {
                call.respond(status = HttpStatusCode.BadRequest, message = "")
                return@post
            }
            val user = userDataSource.getUserByName(request.username)
            if (user == null) {
                call.respond(status = HttpStatusCode.Conflict, "")
                return@post
            }
            val isValidPassword = hashingInterface.verifyHash(
                value = request.password,
                saltedHash = Hash(hash = user.password)
            )
            if (!isValidPassword) {
                call.respond(status = HttpStatusCode.Conflict, "${user.password} - ${ request.password}  ")
                return@post
            }
            val token =
                tokenInterface.generate(config = tokenConfig, TokenClaim(name = "userId", value = user.id.toString()))
            call.respond(status = HttpStatusCode.OK, message = AuthResponse(token = token))
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