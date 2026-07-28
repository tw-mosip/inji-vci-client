package io.mosip.vciclient.common

import org.junit.Assert.assertEquals
import org.junit.Test

class WellKnownUrlTest {

    @Test
    fun `inserts suffix between authority and path per RFC 8414`() {
        assertEquals(
            "https://host.example.com/.well-known/openid-credential-issuer/v1/issuer",
            WellKnownUrl.insertSuffix(
                "https://host.example.com/v1/issuer",
                "/.well-known/openid-credential-issuer"
            )
        )
    }

    @Test
    fun `appends suffix when there is no path`() {
        assertEquals(
            "https://host.example.com/.well-known/oauth-authorization-server",
            WellKnownUrl.insertSuffix(
                "https://host.example.com",
                "/.well-known/oauth-authorization-server"
            )
        )
    }

    @Test
    fun `preserves port and trims trailing path slash`() {
        assertEquals(
            "https://host.example.com:8443/.well-known/openid-configuration/tenant",
            WellKnownUrl.insertSuffix(
                "https://host.example.com:8443/tenant/",
                "/.well-known/openid-configuration"
            )
        )
    }
}
