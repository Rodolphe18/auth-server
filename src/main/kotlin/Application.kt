package com.francotte

import io.ktor.server.application.*
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val mongoDbPassword = System.getenv("MONGO_PWD")
    val mongoDbName = "rodolphefrancotte18"
    val db = KMongo.createClient(connectionString = "mongodb+srv://rodolphefrancotte18:$mongoDbPassword@cluster0.zqu8fvb.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0").coroutine.getDatabase(mongoDbName)
    configureSerialization()
    configureMonitoring()
    configureSecurity()
    configureRouting()
}
