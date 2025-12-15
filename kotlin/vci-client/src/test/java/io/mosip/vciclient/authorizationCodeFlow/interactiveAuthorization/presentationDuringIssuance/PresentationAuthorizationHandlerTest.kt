package io.mosip.vciclient.authorizationCodeFlow.interactiveAuthorization.presentationDuringIssuance

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mosip.openID4VP.OpenID4VP
import io.mosip.openID4VP.authorizationRequest.AuthorizationRequest
import io.mosip.openID4VP.authorizationRequest.presentationDefinition.PresentationDefinition
import io.mosip.openID4VP.authorizationResponse.unsignedVPToken.types.ldp.UnsignedLdpVPToken
import io.mosip.openID4VP.authorizationResponse.vpTokenSigningResult.types.ldp.LdpVPTokenSigningResult
import io.mosip.openID4VP.constants.FormatType
import io.mosip.vciclient.authorizationCodeFlow.interactiveAuthorization.request.AuthorizationRequestData
import io.mosip.vciclient.networkManager.NetworkManager
import io.mosip.vciclient.networkManager.NetworkResponse
import io.mosip.vercred.vcverifier.keyResolver.types.did.DidPublicKeyResolver
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.security.PublicKey

class PresentationAuthorizationHandlerTest {

    private var fakeAuthRequest: AuthorizationRequest? = null

    @Before
    fun setup() {
        mockkObject(NetworkManager)
        mockDidResolver()

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
                id = "pd-id",
                inputDescriptors = emptyList()
            )
        )
    }

    private fun mockDidResolver(): DidPublicKeyResolver {
        val mockResolver = mockk<DidPublicKeyResolver>()

        val fakePublicKey = mockk<PublicKey>(relaxed = true)

        every {
            mockResolver.resolve(any())
        } returns fakePublicKey

        return mockResolver
    }

    // ------------------------------------------------------------------------

    @Test
    fun `should return error when request is not PresentationAuthorizationRequestData`() = runTest {
        val handler = PresentationDuringIssuanceAuthorizationMethodService(
            handlePresentationRequest = { emptyMap() },
            signVerifiablePresentation = { emptyMap() },
        )

        val result = handler.authorizeUser(AuthorizationRequestData())

        Assert.assertEquals("error", result.status)
        Assert.assertEquals("invalid_request", result.error)
        Assert.assertTrue(
            result.errorDescription?.contains("Expected PresentationAuthorizationRequestData") == true
        )
    }

    // ------------------------------------------------------------------------

    @Test
    fun `should fail if credential fetch times out`() = runTest {
        val mockOvp = mockk<OpenID4VP>()

        coEvery {
            mockOvp.authenticateVerifier(authRequest = any(), any(), any())
        } returns fakeAuthRequest!!

        every {
            NetworkManager.sendRequest(any(), any(), any(), any())
        } returns NetworkResponse("""{"status":"success"}""", null)

        val handler = PresentationDuringIssuanceAuthorizationMethodService(
            handlePresentationRequest = {
                delay(600)
                emptyMap()
            },
            signVerifiablePresentation = { emptyMap() },
            openId4vp = mockOvp,
            handlePresentationTimeoutMs = 500L
        )

        val result = handler.authorizeUser(
            OpenId4VpPresentationAuthorizationRequestData(
                ovpRequest = mapOf("issuer" to "https://trusted.com"),
                iar = "https://issuer.com",
                authSession = "auth-session"
            )
        )

        Assert.assertEquals("error", result.status)
        Assert.assertEquals("VCI-011", result.error)
        Assert.assertTrue(
            result.errorDescription?.contains("Timed out") == true
        )
    }

    // ------------------------------------------------------------------------

    @Test
    fun `should fail if signing throws exception`() = runTest {
        val mockOvp = mockk<OpenID4VP>()

        coEvery {
            mockOvp.authenticateVerifier(authRequest = any(), any(), any())
        } returns fakeAuthRequest!!

        coEvery {
            mockOvp.constructUnsignedVPToken(any(), any(), any())
        } returns emptyMap()

        every {
            NetworkManager.sendRequest(any(), any(), any(), any())
        } returns NetworkResponse("""{"status":"success"}""", null)

        val handler = PresentationDuringIssuanceAuthorizationMethodService(
            handlePresentationRequest = {
                mapOf(
                    "id1" to mapOf(
                        FormatType.LDP_VC to listOf(
                            """{ "@context": [ "https://www.w3.org/2018/credentials/v1" ], "id": "urn:uuid:3fa85f64-5717-4562-b3fc-2c963f66afa6", "type": [ "VerifiableCredential" ], "issuer": "https://issuer.example.com", "issuanceDate": "2025-12-13T00:00:00Z", "credentialSubject": { "id": "did:example:abcdef1234567890", "name": "Alice" } } """.trimMargin()
                        )
                    )
                )
            },
            signVerifiablePresentation = {
                throw RuntimeException("Signing failed")
            },
            openId4vp = mockOvp,
            didPublicKeyResolver = mockDidResolver()
        )

        val result = handler.authorizeUser(
            OpenId4VpPresentationAuthorizationRequestData(
                ovpRequest = mapOf("issuer" to "https://trusted.com"),
                iar = "https://issuer.com",
                authSession = "auth-session"
            )
        )
        Assert.assertEquals("error", result.status)
        Assert.assertEquals("VCI-011", result.error)
        Assert.assertTrue(result.errorDescription!!.contains("Signing failed"))
    }

    // ------------------------------------------------------------------------

    @Test
    fun `should return error if authorization request is invalid`() = runTest {
        val mockOvp = mockk<OpenID4VP>()

        coEvery {
            mockOvp.authenticateVerifier(authRequest = any(), any(), any())
        } throws RuntimeException("Invalid request")

        every {
            NetworkManager.sendRequest(any(), any(), any(), any(), any())
        } returns NetworkResponse(
            """{
              "status":"ok",
              "auth_session":"auth-session-xyz"
            }""".trimIndent(),
            null
        )
        val handler = PresentationDuringIssuanceAuthorizationMethodService(
            handlePresentationRequest = { emptyMap() },
            signVerifiablePresentation = { emptyMap() },
            openId4vp = mockOvp
        )

        val result = handler.authorizeUser(
            OpenId4VpPresentationAuthorizationRequestData(
                ovpRequest = mapOf("issuer" to "https://trusted.com"),
                iar = "https://issuer.com",
                authSession = "auth-session"
            )
        )

        Assert.assertEquals("error", result.status)
        Assert.assertEquals("VCI-011", result.error)
        Assert.assertTrue(
            result.errorDescription?.contains("Invalid request") == true
        )
    }

    // ------------------------------------------------------------------------

    @Test
    fun `type should return openid4vp_presentation`() {
        val handler = PresentationDuringIssuanceAuthorizationMethodService(
            handlePresentationRequest = { emptyMap() },
            signVerifiablePresentation = { emptyMap() },
        )

        Assert.assertEquals("openid4vp_presentation", handler.type())
    }

    // ------------------------------------------------------------------------

    @Test
    fun `should successfully authorize and return success response`() = runTest {
        val mockOvp = mockk<OpenID4VP>()

        coEvery {
            mockOvp.authenticateVerifier(authRequest = any(), any(), any())
        } returns fakeAuthRequest!!

        coEvery {
            mockOvp.constructUnsignedVPToken(any(), any(), any())
        } returns mapOf(
            FormatType.LDP_VC to UnsignedLdpVPToken("unsigned")
        )

        coEvery {
            mockOvp.constructVPResponse(any(), any())
        } returns mapOf("vp_token" to "signed")

        every {
            NetworkManager.sendRequest(any(), any(), any(), any(), any())
        } returns NetworkResponse(
            """{
              "status":"success",
              "authorization_code":"auth-code-123",
              "auth_session":"auth-session-xyz"
            }""",
            null
        )

        val handler = PresentationDuringIssuanceAuthorizationMethodService(
            handlePresentationRequest = {
                mapOf(
                    "id1" to mapOf(
                        FormatType.LDP_VC to listOf(
                            """{ "@context": [ "https://www.w3.org/2018/credentials/v1" ], "id": "urn:uuid:3fa85f64-5717-4562-b3fc-2c963f66afa6", "type": [ "VerifiableCredential" ], "issuer": "https://issuer.example.com", "issuanceDate": "2025-12-13T00:00:00Z", "credentialSubject": { "id": "did:example:abcdef1234567890", "name": "Alice" } } """.trimMargin()
                        )
                    )
                )
            },
            signVerifiablePresentation = {
                mapOf(
                    FormatType.LDP_VC to LdpVPTokenSigningResult(
                        "signed-vp-token",
                        null,
                        "Ed25519Signature2018"
                    )
                )
            },
            openId4vp = mockOvp,
            didPublicKeyResolver = mockDidResolver()
        )

        val result = handler.authorizeUser(
            OpenId4VpPresentationAuthorizationRequestData(
                ovpRequest = mapOf("issuer" to "https://trusted.com"),
                iar = "https://issuer.com",
                authSession = "auth-session-xyz"
            )
        )
        Assert.assertEquals("success", result.status)
        Assert.assertEquals("auth-code-123", result.authorizationCode)
        Assert.assertEquals("auth-session-xyz", result.authSession)

        coVerify(exactly = 1) { mockOvp.authenticateVerifier(authRequest = any(), any(), false) }
        coVerify(exactly = 1) { mockOvp.constructUnsignedVPToken(any(), any(), any()) }
        coVerify(exactly = 1) { mockOvp.constructVPResponse(any(), any()) }
    }

    @Test
    fun `should fail if no credentials selected by user`() = runTest {
        val mockOvp = mockk<OpenID4VP>()

        coEvery {
            mockOvp.authenticateVerifier(authRequest = any(), any(), any())
        } returns fakeAuthRequest!!

        every {
            NetworkManager.sendRequest(any(), any(), any(), any(), any())
        } returns NetworkResponse(
            """{ "status": "error" }""",
            null
        )

        val handler = PresentationDuringIssuanceAuthorizationMethodService(
            handlePresentationRequest = { emptyMap() }, // 👈 user selected nothing
            signVerifiablePresentation = { emptyMap() },
            openId4vp = mockOvp
        )

        val result = handler.authorizeUser(
            OpenId4VpPresentationAuthorizationRequestData(
                ovpRequest = mapOf("issuer" to "https://trusted.com"),
                iar = "https://issuer.com",
                authSession = "auth-session"
            )
        )

        Assert.assertEquals("error", result.status)
        Assert.assertEquals("VCI-011", result.error)
        Assert.assertTrue(result.errorDescription!!.contains("No credentials selected by user"))
    }

    @Test
    fun `should fail if credentialSubject is missing`() = runTest {
        val mockOvp = mockk<OpenID4VP>()

        coEvery {
            mockOvp.authenticateVerifier(authRequest = any(), any(), any())
        } returns fakeAuthRequest!!

        every {
            NetworkManager.sendRequest(any(), any(), any(), any(), any())
        } returns NetworkResponse(
            """{ "status": "error" }""",
            null
        )

        val invalidVc = """
        {
          "@context": ["https://www.w3.org/2018/credentials/v1"],
          "type": ["VerifiableCredential"],
          "issuer": "https://issuer.example.com"
        }
    """.trimIndent()

        val handler = PresentationDuringIssuanceAuthorizationMethodService(
            handlePresentationRequest = {
                mapOf("id1" to mapOf(FormatType.LDP_VC to listOf(invalidVc)))
            },
            signVerifiablePresentation = { emptyMap() },
            openId4vp = mockOvp
        )

        val result = handler.authorizeUser(
            OpenId4VpPresentationAuthorizationRequestData(
                ovpRequest = mapOf("issuer" to "https://trusted.com"),
                iar = "https://issuer.com",
                authSession = "auth-session"
            )
        )

        Assert.assertEquals("error", result.status)
        Assert.assertTrue(result.errorDescription!!.contains("Missing credentialSubject"))
    }

    @Test
    fun `should fail if credentialSubject id is missing`() = runTest {
        val mockOvp = mockk<OpenID4VP>()

        coEvery {
            mockOvp.authenticateVerifier(authRequest = any(), any(), any())
        } returns fakeAuthRequest!!

        every {
            NetworkManager.sendRequest(any(), any(), any(), any(), any())
        } returns NetworkResponse(
            """{ "status": "error" }""",
            null
        )

        val invalidVc = """
        {
          "credentialSubject": {
            "name": "Alice"
          }
        }
    """.trimIndent()

        val handler = PresentationDuringIssuanceAuthorizationMethodService(
            handlePresentationRequest = {
                mapOf("id1" to mapOf(FormatType.LDP_VC to listOf(invalidVc)))
            },
            signVerifiablePresentation = { emptyMap() },
            openId4vp = mockOvp
        )

        val result = handler.authorizeUser(
            OpenId4VpPresentationAuthorizationRequestData(
                ovpRequest = mapOf("issuer" to "https://trusted.com"),
                iar = "https://issuer.com",
                authSession = "auth-session"
            )
        )

        Assert.assertEquals("error", result.status)
        Assert.assertTrue(result.errorDescription!!.contains("Missing credentialSubject.id"))
    }

    @Test
    fun `should fail if credentialSubject id is blank`() = runTest {
        val mockOvp = mockk<OpenID4VP>()

        coEvery {
            mockOvp.authenticateVerifier(authRequest = any(), any(), any())
        } returns fakeAuthRequest!!

        every {
            NetworkManager.sendRequest(any(), any(), any(), any(), any())
        } returns NetworkResponse(
            """{ "status": "error" }""",
            null
        )

        val invalidVc = """
        {
          "credentialSubject": {
            "id": "   "
          }
        }
    """.trimIndent()

        val handler = PresentationDuringIssuanceAuthorizationMethodService(
            handlePresentationRequest = {
                mapOf("id1" to mapOf(FormatType.LDP_VC to listOf(invalidVc)))
            },
            signVerifiablePresentation = { emptyMap() },
            openId4vp = mockOvp
        )

        val result = handler.authorizeUser(
            OpenId4VpPresentationAuthorizationRequestData(
                ovpRequest = mapOf("issuer" to "https://trusted.com"),
                iar = "https://issuer.com",
                authSession = "auth-session"
            )
        )

        Assert.assertEquals("error", result.status)
        Assert.assertTrue(result.errorDescription!!.contains("must not be blank"))
    }

    @Test
    fun `should fail if did resolver throws exception`() = runTest {
        val mockOvp = mockk<OpenID4VP>()
        val failingResolver = mockk<DidPublicKeyResolver>()

        coEvery {
            mockOvp.authenticateVerifier(authRequest = any(), any(), any())
        } returns fakeAuthRequest!!
        every {
            NetworkManager.sendRequest(any(), any(), any(), any(), any())
        } returns NetworkResponse(
            """{ "status": "error" }""",
            null
        )

        every {
            failingResolver.resolve(any())
        } throws RuntimeException("DID not found")

        val validVc = """
        {
          "credentialSubject": {
            "id": "did:example:123"
          }
        }
    """.trimIndent()

        val handler = PresentationDuringIssuanceAuthorizationMethodService(
            handlePresentationRequest = {
                mapOf("id1" to mapOf(FormatType.LDP_VC to listOf(validVc)))
            },
            signVerifiablePresentation = { emptyMap() },
            openId4vp = mockOvp,
            didPublicKeyResolver = failingResolver
        )

        val result = handler.authorizeUser(
            OpenId4VpPresentationAuthorizationRequestData(
                ovpRequest = mapOf("issuer" to "https://trusted.com"),
                iar = "https://issuer.com",
                authSession = "auth-session"
            )
        )

        Assert.assertEquals("error", result.status)
        Assert.assertTrue(result.errorDescription!!.contains("Failed to resolve public key"))
    }

    @Test
    fun `should fallback to JsonWebSignature2020 for non Ed25519 key`() = runTest {
        val mockOvp = mockk<OpenID4VP>()
        val resolver = mockk<DidPublicKeyResolver>()
        val publicKey = mockk<PublicKey>()

        every { publicKey.algorithm } returns "RSA"
        every { resolver.resolve(any()) } returns publicKey

        coEvery {
            mockOvp.authenticateVerifier(authRequest = any(), any(), any())
        } returns fakeAuthRequest!!

        coEvery {
            mockOvp.constructUnsignedVPToken(any(), any(), "JsonWebSignature2020")
        } returns emptyMap()

        coEvery {
            mockOvp.constructVPResponse(any(), any())
        } returns mapOf("vp" to "token")

        every {
            NetworkManager.sendRequest(any(), any(), any(), any(), any())
        } returns NetworkResponse("""{"status":"success"}""", null)

        val handler = PresentationDuringIssuanceAuthorizationMethodService(
            handlePresentationRequest = {
                mapOf(
                    "id1" to mapOf(
                        FormatType.LDP_VC to listOf(
                            """{ "credentialSubject": { "id": "did:example:rsa" } }"""
                        )
                    )
                )
            },
            signVerifiablePresentation = { emptyMap() },
            openId4vp = mockOvp,
            didPublicKeyResolver = resolver
        )

        val result = handler.authorizeUser(
            OpenId4VpPresentationAuthorizationRequestData(
                ovpRequest = mapOf("issuer" to "https://trusted.com"),
                iar = "https://issuer.com",
                authSession = "auth-session"
            )
        )

        Assert.assertEquals("success", result.status)
    }

    @Test
    fun `should use Ed25519Signature2020 for Ed25519 public key`() = runTest {
        val mockOvp = mockk<OpenID4VP>()
        val resolver = mockk<DidPublicKeyResolver>()
        val publicKey = mockk<PublicKey>()

        every { publicKey.algorithm } returns "Ed25519"
        every { resolver.resolve(any()) } returns publicKey

        coEvery {
            mockOvp.authenticateVerifier(authRequest = any(), any(), any())
        } returns fakeAuthRequest!!

        coEvery {
            mockOvp.constructUnsignedVPToken(
                verifiableCredentials = any(),
                holderId = any(),
                signatureSuite = "Ed25519Signature2020"
            )
        } returns emptyMap()

        coEvery {
            mockOvp.constructVPResponse(any(), any())
        } returns mapOf("vp" to "token")

        every {
            NetworkManager.sendRequest(any(), any(), any(), any(), any())
        } returns NetworkResponse("""{"status":"success"}""", null)

        val handler = PresentationDuringIssuanceAuthorizationMethodService(
            handlePresentationRequest = {
                mapOf(
                    "id1" to mapOf(
                        FormatType.LDP_VC to listOf(
                            """{ "credentialSubject": { "id": "did:example:ed25519" } }"""
                        )
                    )
                )
            },
            signVerifiablePresentation = { emptyMap() },
            openId4vp = mockOvp,
            didPublicKeyResolver = resolver
        )

        val result = handler.authorizeUser(
            OpenId4VpPresentationAuthorizationRequestData(
                ovpRequest = mapOf("issuer" to "https://trusted.com"),
                iar = "https://issuer.com",
                authSession = "auth-session"
            )
        )

        Assert.assertEquals("success", result.status)

        coVerify(exactly = 1) {
            mockOvp.constructUnsignedVPToken(any(), any(), "Ed25519Signature2020")
        }
    }


}