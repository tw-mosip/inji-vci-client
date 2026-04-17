package io.mosip.vciclient.proof.jwt

import io.mosip.vciclient.constants.ProofType
import org.junit.Assert.assertEquals
import org.junit.Test

class JWTProofTest {
    @Test
    fun `should keep proof type fixed and store jwt as data`() {
        val proof = JWTProof(jwt = "header.payload.signature")

        assertEquals(ProofType.JWT.value, proof.proofType)
        assertEquals("header.payload.signature", proof.jwt)
    }

    @Test
    fun `should allow empty jwt by default`() {
        val proof = JWTProof()

        assertEquals("", proof.jwt)
    }
}
