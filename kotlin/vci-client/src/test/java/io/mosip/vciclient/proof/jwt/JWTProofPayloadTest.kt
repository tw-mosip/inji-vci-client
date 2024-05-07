package io.mosip.vciclient.proof.jwt

import io.mosip.vciclient.proof.jwt.JWTProofPayload
import org.junit.Assert.*
import org.junit.Test

class JWTProofPayloadTest {
    @Test
    fun `should construct payload with issuer, nonce,audience, issuance and expiration compulsory`() {
        val jwtProofPayLoad: String = JWTProofPayload(
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
}""", jwtProofPayLoad
        )
    }
}