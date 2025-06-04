package com.francotte

import com.francotte.data.MongoDataSource
import com.francotte.security.hashing.SHA256HashingService
import com.francotte.security.token.JwtTokenService
import com.francotte.security.token.TokenConfig
import io.ktor.server.application.*
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val mongoDbPassword = System.getenv("MONGO_PWD")
    val mongoDbName = "rodolphefrancotte18"
    val db = KMongo.createClient(connectionString = "mongodb+srv://rodolphe18:$mongoDbPassword@cluster0.zqu8fvb.mongodb.net/$mongoDbName?retryWrites=true&w=majority&appName=Cluster0").coroutine.getDatabase(mongoDbName)
    val userDataSource = MongoDataSource(db)
    val tokenService = JwtTokenService()
    val tokenConfig = TokenConfig(issuer = environment.config.property("jwt.issuer").getString(), audience = environment.config.property("jwt.audience").getString(), expiresIn = 365L * 1000L * 60L * 60L * 24L, secret = System.getenv("JWT_SECRET"))
    val hashingService = SHA256HashingService()
    configureSerialization()
    configureMonitoring()
    configureSecurity(tokenConfig)
    configureRouting(userDataSource,userDataSource, hashingService, tokenService, tokenConfig, "")
}
