package com.francotte.security.hashing

interface HashingInterface {

    fun generateHash(value:String): Hash
    fun verifyHash(value: String, hash: Hash): Boolean

}