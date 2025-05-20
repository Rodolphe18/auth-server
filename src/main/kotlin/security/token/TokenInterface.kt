package com.francotte.security.token

interface TokenInterface {

    fun generate(config: TokenConfig, vararg claims:TokenClaim):String
}