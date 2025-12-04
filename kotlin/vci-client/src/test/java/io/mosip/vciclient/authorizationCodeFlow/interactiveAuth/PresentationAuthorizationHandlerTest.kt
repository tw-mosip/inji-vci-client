package io.mosip.vciclient.authorizationCodeFlow.interactiveAuth

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mosip.openID4VP.OpenID4VP
import io.mosip.openID4VP.authorizationRequest.AuthorizationRequest
import io.mosip.openID4VP.authorizationRequest.Verifier
import io.mosip.openID4VP.authorizationRequest.presentationDefinition.PresentationDefinition
import io.mosip.openID4VP.authorizationResponse.unsignedVPToken.types.ldp.UnsignedLdpVPToken
import io.mosip.openID4VP.authorizationResponse.vpTokenSigningResult.types.ldp.LdpVPTokenSigningResult
import io.mosip.openID4VP.constants.FormatType
import io.mosip.vciclient.networkManager.NetworkManager
import io.mosip.vciclient.networkManager.NetworkResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PresentationAuthorizationHandlerTest {

    var fakeAuthRequest: AuthorizationRequest? = null

    @Before
    fun setup() {
        mockkObject(NetworkManager)
        fakeAuthRequest = AuthorizationRequest(
            clientId = "https://trusted.com",
            redirectUri = "",
            responseType = "",
            state = "",
            nonce = "",
            responseMode = "",
            responseUri = null,
            walletNonce = "",
            clientMetadata = null,
            clientIdScheme = null,
            presentationDefinition = PresentationDefinition(
                id = "pd-id", inputDescriptors = listOf()
            )
        )
    }

    @Test
    fun `should return error when request is not PresentationAuthorizationRequestData`() = runTest {
        val handler = PresentationAuthorizationHandler(
            handlePresentationRequest = { emptyMap() },
            signVerifiablePresentation = { emptyMap() },
            trustedVerifiers = listOf()
        )

        val result = handler.authorizeUser(AuthorizationRequestData())

        assertEquals("error", result.status)
        assertEquals("invalid_request", result.error)
        assertTrue(result.errorDescription?.contains("Expected PresentationAuthorizationRequestData") == true)
    }

    @Test
    fun `should fail if credential fetch times out`() = runTest {
        val mockOvp = mockk<OpenID4VP>()


        coEvery {
            mockOvp.authenticateVerifier(authRequest = any(), any())
        } returns fakeAuthRequest!!

        val handler = PresentationAuthorizationHandler(
            handlePresentationRequest = {
            delay(600) // > 500 sec timeout in handler
            emptyMap()
        }, signVerifiablePresentation = { emptyMap() }, trustedVerifiers = listOf(
            Verifier("https://trusted.com", responseUris = listOf(), jwksUri = "")
        ), openId4vp = mockOvp, handlePresentationTimeoutMs = 500L
        )

        val result = handler.authorizeUser(
            PresentationAuthorizationRequestData(
                ovpRequest = mapOf("issuer" to "https://trusted.com"),
                iar = "https://issuer.com",
                authSession = "auth-session"
            )
        )

        assertEquals("error", result.status)
        assertEquals("VCI-011", result.error)
        assertTrue(result.errorDescription?.contains("Failed to fetch matching credentials. Timed out waiting for") == true)
    }

    @Test
    fun `should fail if signing throws exception`() = runTest {
        val mockOvp = mockk<OpenID4VP>()
        coEvery {
            mockOvp.authenticateVerifier(authRequest = any(), any())
        } returns fakeAuthRequest!!

        coEvery {
            mockOvp.constructUnsignedVPToken(
                verifiableCredentials = any(), holderId = any(), signatureSuite = any()
            )
        } returns emptyMap()
        val handler = PresentationAuthorizationHandler(
            handlePresentationRequest = {
            mapOf("id1" to mapOf(FormatType.LDP_VC to listOf("data")))
        }, signVerifiablePresentation = {
            throw RuntimeException("Signing failed")
        }, trustedVerifiers = listOf(), openId4vp = mockOvp
        )

        val result = handler.authorizeUser(
            PresentationAuthorizationRequestData(
                ovpRequest = mapOf("issuer" to "https://trusted.com"),
                iar = "https://issuer.com",
                authSession = "auth-session"
            )
        )

        assertEquals("error", result.status)
        assertEquals("VCI-011", result.error)
        print(result)
        assertTrue(result.errorDescription!!.contains("Signing failed"))
    }

    @Test
    fun `should return error if authorization request is invalid`() = runTest {
        val mockOvp = mockk<OpenID4VP>()
        coEvery {
            mockOvp.authenticateVerifier(authRequest = any(), any())
        } throws RuntimeException("Invalid request")

        val handler = PresentationAuthorizationHandler(
            handlePresentationRequest = { emptyMap() },
            signVerifiablePresentation = { emptyMap() },
            trustedVerifiers = listOf(
                Verifier("https://trusted.com", responseUris = listOf(), jwksUri = "")
            ),
            openId4vp = mockOvp
        )

        val result = handler.authorizeUser(
            PresentationAuthorizationRequestData(
                ovpRequest = mapOf("issuer" to "https://trusted.com"),
                iar = "https://issuer.com",
                authSession = "auth-session"
            )
        )

        assertEquals("error", result.status)
        assertEquals("VCI-011", result.error)
        assertTrue(result.errorDescription?.contains("Verifier is not trusted or request is invalid. Invalid request") == true)
    }

    @Test
    fun `type should return redirect_to_presentation`() {
        val handler = PresentationAuthorizationHandler(
            handlePresentationRequest = { emptyMap() },
            signVerifiablePresentation = { emptyMap() },
            trustedVerifiers = listOf()
        )

        assertEquals("openid4vp_presentation", handler.type())
    }

    //integration test
    @Test
    fun `should return error if authorization request is malformed`() = runTest {
        val handler = PresentationAuthorizationHandler(
            handlePresentationRequest = { emptyMap() },
            signVerifiablePresentation = { emptyMap() },
            trustedVerifiers = listOf()
        )

        val result = handler.authorizeUser(
            PresentationAuthorizationRequestData(
                ovpRequest = mapOf("invalid_key" to 123),
                iar = "https://issuer.com",
                authSession = "auth-session"
            )
        )

        assertEquals("error", result.status)
        assertEquals("VCI-011", result.error)
        assertTrue(result.errorDescription?.contains("Missing Input: client_id param is required") == true)
    }

    @Test
    fun `should successfully authenticate verifier if auth request is valid`() = runTest {
        val spiedOvp = spyk(OpenID4VP(traceabilityId = "", walletMetadata = null))

        coEvery {
            spiedOvp.constructUnsignedVPToken(any(), any(), any())
        } returns mapOf(FormatType.LDP_VC to UnsignedLdpVPToken("data-to-sign"))

        coEvery {
            spiedOvp.constructVPResponse(any(), any())
        } returns mapOf("id" to "vp-token")

        val handler = PresentationAuthorizationHandler(
            handlePresentationRequest = { mapOf("id1" to mapOf(FormatType.LDP_VC to listOf("data"))) },
            signVerifiablePresentation = {
                mapOf(
                    FormatType.LDP_VC to LdpVPTokenSigningResult(
                        "signed-vp-token", signatureAlgorithm = "demo"
                    )
                )
            },
            trustedVerifiers = listOf(
                Verifier("https://trusted.com", emptyList(), "")
            ),
            openId4vp = spiedOvp
        )

        val authRequest = mapOf(
            "client_id" to "redirect_uri:https://2339e926555d.ngrok-free.app/verifier/vp-response",
            "presentation_definition_uri" to "https://2339e926555d.ngrok-free.app/verifier/presentation_definition_uri",
            "response_type" to "vp_token",
            "response_mode" to "iar_post",
            "nonce" to "4IvfsO4S78tcjPVQQg7QNA==",
            "state" to "IzVoGD5xFRmSERI5ARu6GA==",
            "response_uri" to "https://2339e926555d.ngrok-free.app/verifier/vp-response",
            "client_metadata" to mapOf(
                "client_name" to "Requester name",
                "logo_uri" to "https://mosip.github.io/inji-config/logos/StayProtectedInsurance.png",
                "authorization_encrypted_response_alg" to "ECDH-ES",
                "authorization_encrypted_response_enc" to "A256GCM",
                "jwks" to mapOf(
                    "keys" to listOf(
                        mapOf(
                            "kty" to "OKP",
                            "crv" to "X25519",
                            "use" to "enc",
                            "x" to "BVNVdqorpxCCnTOkkw8S2NAYXvfEvkC-8RDObhrAUA4",
                            "alg" to "ECDH-ES",
                            "kid" to "verifier-key-id"
                        )
                    )
                ),
                "vp_formats" to mapOf(
                    "mso_mdoc" to mapOf(
                        "alg" to listOf("ES256")
                    ), "ldp_vp" to mapOf(
                        "proof_type" to listOf(
                            "Ed25519Signature2018", "Ed25519Signature2020", "RsaSignature2018"
                        )
                    )
                )

            )
        )

        every {
            NetworkManager.sendRequest(
                any(), any(), any(), any(), any()
            )
        } returns NetworkResponse(
            """
                {
                    status: "success"
                }
            """.trimIndent(), headers = null
        )

        val result = handler.authorizeUser(
            PresentationAuthorizationRequestData(
                ovpRequest = authRequest, iar = "https://issuer.com", authSession = "auth-session"
            )
        )

        print(result)
        assertEquals("success", result.status)

        coVerify(exactly = 1) {
            spiedOvp.authenticateVerifier(authRequest = any(), any())
        }

        coVerify(exactly = 1) {
            spiedOvp.constructUnsignedVPToken(any(), any(), any())
        }
    }
}