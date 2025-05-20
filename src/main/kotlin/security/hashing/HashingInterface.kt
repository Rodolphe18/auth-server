package com.francotte.security.hashing

interface HashingInterface {

    fun generateSaltedHash(value: String, saltLength: Int = 32): SaltedHash
    fun verifyHash(value: String, saltedHash: SaltedHash): Boolean

}