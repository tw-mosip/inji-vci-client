package io.mosip.vciclient.exception

import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.Test

class VCIClientExceptionTest {

    @Test
    fun `should retain root source error code from nested vci exceptions`() {
        val root = AuthorizationServerDiscoveryException("resolver failure")
        val wrapped = NetworkRequestFailedException(
            message = "token endpoint failed",
            issuerErrorCode = "server_error",
            issuerErrorDescription = "temporary issue",
            cause = root
        )

        assertEquals("VCI-001", wrapped.code)
        assertEquals("server_error", wrapped.issuerErrorCode)
        assertEquals("temporary issue", wrapped.issuerErrorDescription)
        assertEquals(
            "Network request failed, details - token endpoint failed",
            wrapped.message
        )
    }

    @Test
    fun `should construct invalid access token exception with server details`() {
        val exception = InvalidAccessTokenException(
            message = "expired",
            issuerErrorCode = "invalid_token",
            issuerErrorDescription = "token expired",
            cause = IllegalStateException("expired")
        )

        assertEquals("VCI-003", exception.code)
        assertEquals("invalid_token", exception.issuerErrorCode)
        assertEquals("token expired", exception.issuerErrorDescription)
        assertEquals("Access token is invalid - expired", exception.message)
    }

    @Test
    fun `should construct invalid data provided exception without server details`() {
        val exception = InvalidDataProvidedException("credential issuer missing")

        assertEquals("VCI-004", exception.code)
        assertNull(exception.issuerErrorCode)
        assertNull(exception.issuerErrorDescription)
        assertEquals(
            "Required details not provided credential issuer missing",
            exception.message
        )
    }

    @Test
    fun `should construct authorization server discovery exception with server details`() {
        val exception = AuthorizationServerDiscoveryException(
            message = "metadata endpoint unavailable",
            issuerErrorCode = "temporarily_unavailable",
            issuerErrorDescription = "retry later"
        )

        assertEquals("VCI-001", exception.code)
        assertEquals("temporarily_unavailable", exception.issuerErrorCode)
        assertEquals("retry later", exception.issuerErrorDescription)
        assertEquals(
            "Failed to discover authorization server : metadata endpoint unavailable",
            exception.message
        )
    }

    @Test
    fun `should construct network request failed exception without server details`() {
        val exception = NetworkRequestFailedException("connection reset")

        assertEquals("VCI-006", exception.code)
        assertNull(exception.issuerErrorCode)
        assertNull(exception.issuerErrorDescription)
        assertEquals(
            "Network request failed, details - connection reset",
            exception.message
        )
    }
}
