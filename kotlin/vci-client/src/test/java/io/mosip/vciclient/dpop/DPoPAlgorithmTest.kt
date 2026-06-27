package io.mosip.vciclient.dpop

import org.junit.Assert.assertEquals
import org.junit.Test

class DPoPAlgorithmTest {

    @Test
    fun `defaults to ES256 when list is null or empty`() {
        assertEquals(DPoPAlgorithm.ES256, DPoPAlgorithm.select(null))
        assertEquals(DPoPAlgorithm.ES256, DPoPAlgorithm.select(emptyList()))
    }

    @Test
    fun `prefers EdDSA when multiple supported algorithms are advertised`() {
        assertEquals(
            DPoPAlgorithm.EDDSA,
            DPoPAlgorithm.select(listOf("RS256", "ES256", "ES256K", "EdDSA"))
        )
    }

    @Test
    fun `honours preference order ES256K over ES256`() {
        assertEquals(DPoPAlgorithm.ES256K, DPoPAlgorithm.select(listOf("ES512", "ES256K", "ES256")))
    }

    @Test
    fun `selects RSA when it is the only supported algorithm`() {
        assertEquals(DPoPAlgorithm.RS256, DPoPAlgorithm.select(listOf("RS256", "PS512")))
    }

    @Test
    fun `falls back to ES256 when only unsupported algorithms are advertised`() {
        assertEquals(DPoPAlgorithm.ES256, DPoPAlgorithm.select(listOf("PS256", "HS256")))
    }

    @Test
    fun `every supported algorithm generates a key and signer`() {
        DPoPAlgorithm.values().forEach { algorithm ->
            val key = algorithm.generateKey()
            assertEquals(false, key.toPublicJWK().isPrivate)
            // signer construction must not throw for the generated key
            algorithm.signer(key)
        }
    }
}
