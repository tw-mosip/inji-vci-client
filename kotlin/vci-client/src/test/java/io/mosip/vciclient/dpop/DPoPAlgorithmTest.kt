package io.mosip.vciclient.dpop

import org.junit.Assert.assertEquals
import org.junit.Test

class DPoPAlgorithmTest {

    @Test
    fun `defaults to ES256 when list is null`() {
        assertEquals(DPoPAlgorithm.ES256, DPoPAlgorithm.select(null))
    }

    @Test
    fun `defaults to ES256 when list is empty`() {
        assertEquals(DPoPAlgorithm.ES256, DPoPAlgorithm.select(emptyList()))
    }

    @Test
    fun `prefers ES256 when multiple supported algorithms are advertised`() {
        assertEquals(DPoPAlgorithm.ES256, DPoPAlgorithm.select(listOf("ES512", "ES384", "ES256")))
    }

    @Test
    fun `selects the only supported elliptic curve algorithm advertised`() {
        assertEquals(DPoPAlgorithm.ES384, DPoPAlgorithm.select(listOf("RS256", "ES384")))
    }

    @Test
    fun `falls back to ES256 when only unsupported algorithms are advertised`() {
        assertEquals(DPoPAlgorithm.ES256, DPoPAlgorithm.select(listOf("RS256", "PS256")))
    }
}
