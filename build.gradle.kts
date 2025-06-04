
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
    id("com.gradleup.shadow") version "9.0.0-beta13"
}

group = "com.francotte"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.kmongo)
    implementation(libs.kmongo.coroutine)
    implementation(libs.codec)
    implementation(libs.logback.classic)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
}

val remoteHost = "root@46.202.170.205"
val remotePath = "/root/jwtauth/jwtauth.jar"
val jarLocalPath = "$buildDir/libs/auth-server-all.jar"
val remoteService = "jwtauth.service"
val sshKeyPath = "keys/id_rsa"

val deployJar by tasks.registering(Exec::class) {
    dependsOn("shadowJar")
    group = "deployment"
    description = "Build le JAR, le déploie et redémarre le service distant"

    commandLine("bash", "-c", """
        scp -i $sshKeyPath -o StrictHostKeyChecking=no $jarLocalPath $remoteHost:$remotePath && \
        ssh -i $sshKeyPath -o StrictHostKeyChecking=no $remoteHost 'systemctl restart $remoteService && systemctl status $remoteService --no-pager'
    """.trimIndent())
}