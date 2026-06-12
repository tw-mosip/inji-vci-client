package io.mosip.vciclient.authorizationCodeFlow.interactiveAuthorization.presentationDuringIssuance

import io.mosip.vciclient.common.JsonUtils
import org.junit.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertNotNull

class PresentationInteractionResponseTest {

    @Test
    fun `should accept request uri for library validation`() {
        val response = validResponse(
            openid4vpRequest = mapOf(
                "request_uri" to "https://verifier.example.com/request/123"
            )
        )

        assertDoesNotThrow {
            response.validate()
        }
    }

    @Test
    fun `should leave signed request validation to OpenID4VP library`() {
        val response = validResponse(
            openid4vpRequest = mapOf("request" to "signed-request-jwt")
        )

        assertDoesNotThrow {
            response.validate()
        }
    }

    @Test
    fun `should reject empty OpenID4VP request`() {
        val response = validResponse(openid4vpRequest = emptyMap())

        assertThrows<IllegalArgumentException> {
            response.validate()
        }
    }

    private fun validResponse(openid4vpRequest: Map<String, Any>): PresentationInteractionResponse {
        val responseJson = JsonUtils.serialize(
            mapOf(
                "status" to "require_interaction",
                "type" to "openid4vp_presentation",
                "auth_session" to "auth-session",
                "openid4vp_request" to openid4vpRequest
            )
        )

        return assertNotNull(
            JsonUtils.deserialize(
                responseJson,
                PresentationInteractionResponse::class.java
            )
        )
    }
}
