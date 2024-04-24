package io.mosip.vciclient.jwt

import org.junit.Assert.*
import org.junit.Test

class JWTPayloadTest {
    @Test
    fun `should construct payload with issuer, nonce,audience, issuance and expiration compulsory`() {
        val jwtPayLoad: String = JWTPayload(
            "45453XDF",
            "O0Ar8K73Ws3Ors9NzlO",
            "https://domai.env.net",
            1713232631,
            1713250631
        ).build()

        assertEquals(
            """{
    "iss": "45453XDF",
    "aud": "https://domai.env.net",
    "exp": 1713250631,
    "nonce": "O0Ar8K73Ws3Ors9NzlO",
    "iat": 1713232631
}""", jwtPayLoad
        )
    }
}