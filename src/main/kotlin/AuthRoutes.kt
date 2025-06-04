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
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.eq
import kotlin.random.Random


fun Route.signUp(
    hashingInterface: HashingInterface, userDataSource: UserDataSource, tokenInterface: TokenInterface,
    tokenConfig: TokenConfig
) {
    post("users/create") {
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
            val userId = generateUniqueUserId(userDataSource.getUsers())
            val user = User(userId = userId, username = request.username, password = hash.hash)
            val wasAcknowledged = userDataSource.insertUser(user)
            if (!wasAcknowledged) {
                call.respond(status = HttpStatusCode.Conflict, "")
                return@post
            }
            val token =
                tokenInterface.generate(
                    config = tokenConfig,
                    TokenClaim(name = "userId", value = user.userId.toString())
                )

            call.respond(
                status = HttpStatusCode.OK,
                AuthResponse(token = token, user = User(user.userId, user.username))
            )
        }

    }
}

fun Route.signIn(
    hashingInterface: HashingInterface,
    userDataSource: UserDataSource,
    token: String
) {

    post("users/auth") {
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
                hash = Hash(hash = user.password)
            )
            if (!isValidPassword) {
                call.respond(status = HttpStatusCode.Conflict, "${user.password} - ${request.password}  ")
                return@post
            }
            call.respond(
                status = HttpStatusCode.Created,
                message = AuthResponse(token = token, user = User(user.userId, user.username))
            )
        }
    }
}

fun Route.deleteUser(userDataSource: UserDataSource) {

    delete("users/{userId}") {
        with(Dispatchers.IO) {
            val userIdParam = call.parameters["userId"]
            val userId = userIdParam?.toLongOrNull()
            if (userId == null) {
                call.respond(HttpStatusCode.BadRequest, "Paramètre userId invalide ou absent.")
                return@delete
            }
            val wasDeleted = userDataSource.deleteUserById(userId)
            if (wasDeleted) {
                call.respond(HttpStatusCode.OK, "Utilisateur supprimé avec succès.")
            } else {
                call.respond(HttpStatusCode.NotFound, "Utilisateur non trouvé.")
            }
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

suspend fun generateUniqueUserId(userCollection: CoroutineCollection<User>): Long {
    var id: Long
    do {
        id = Random.nextLong(1_000_000_000L, 9_999_999_999L)
    } while (userCollection.find(User::userId eq id).first() != null)
    return id
}
