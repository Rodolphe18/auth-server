package com.francotte.security.hashing

import org.apache.commons.codec.digest.DigestUtils

class SHA256HashingService:HashingInterface {
    override fun generateHash(value:String): Hash {

        val hash = DigestUtils.sha256Hex(value)
        return Hash(hash = hash)
    }

    override fun verifyHash(value:String, hash: Hash): Boolean {
        return DigestUtils.sha256Hex(value) == hash.hash
    }
}