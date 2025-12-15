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
import io.mosip.vciclient.exception.InteractiveAuthorizationException
import io.mosip.vciclient.networkManager.NetworkManager
import io.mosip.vciclient.networkManager.NetworkResponse
import io.mosip.vercred.vcverifier.keyResolver.types.did.DidPublicKeyResolver
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import java.security.PublicKey

class PresentationAuthorizationHandlerTest {

    private lateinit var fakeAuthRequest: AuthorizationRequest
    private lateinit var mockOvp: OpenID4VP

    @Before
    fun setup() {
        mockkObject(NetworkManager)

        mockOvp = mockk(relaxed = true)

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

        coEvery {
            mockOvp.authenticateVerifier(authRequest = any(), any(), any())
        } returns fakeAuthRequest

        every {
            NetworkManager.sendRequest(any(), any(), any(), any())
        } returns NetworkResponse("""{"status":"error"}""", null)
    }

    private fun validRequest() =
        PresentationDuringIssuanceRequestData(
            ovpRequest = mapOf("issuer" to "https://trusted.com"),
            iar = "https://issuer.com",
            authSession = "auth-session"
        )

    private fun validCredentialMap() =
        mapOf(
            "id1" to mapOf(
                FormatType.LDP_VC to listOf(
                    """{ "credentialSubject": { "id": "did:example:123" } }"""
                )
            )
        )

    private fun mockDidResolver(): (String) -> PublicKey {
        val resolver = mockk<DidPublicKeyResolver>()
        val publicKey = mockk<PublicKey>(relaxed = true)

        every { resolver.resolve(any()) } returns publicKey

        return { resolver.resolve(it) }
    }

    // ------------------------------------------------------------------------

    @Test
    fun `should throw and send error when request type is invalid`() = runTest {
        val handler = PresentationDuringIssuanceAuthorizationMethodService(
            selectCredentialsForPresentation = { emptyMap() },
            signVerifiablePresentation = { emptyMap() },
            traceabilityId = "test-trace-id",
        )

        assertThrows<InteractiveAuthorizationException> {
            handler.authorizeUser(AuthorizationRequestData())
        }

    }

    @Test
    fun `should successfully authorize and return success response`() = runTest {
        coEvery {
            mockOvp.constructUnsignedVPToken(any(), any(), any())
        } returns mapOf(
            FormatType.LDP_VC to UnsignedLdpVPToken("unsigned")
        )

        coEvery {
            mockOvp.constructVPResponse(any(), any())
        } returns mapOf("vp_token" to "signed")

        every {
            NetworkManager.sendRequest(any(), any(), any(), any())
        } returns NetworkResponse(
            """{
              "status":"success",
              "code":"auth-code-123",
              "auth_session":"auth-session"
            }""",
            null
        )

        val handler = PresentationDuringIssuanceAuthorizationMethodService(
            selectCredentialsForPresentation = { validCredentialMap() },
            signVerifiablePresentation = {
                mapOf(
                    FormatType.LDP_VC to LdpVPTokenSigningResult(
                        "signed-vp-token",
                        null,
                        "Ed25519Signature2020"
                    )
                )
            },
            openId4vp = mockOvp,
            didPublicKeyResolver = mockDidResolver(),
            traceabilityId = "test-trace-id"
        )

        val result = handler.authorizeUser(validRequest())

        Assert.assertEquals("success", result.status)
        Assert.assertEquals("auth-code-123", result.authorizationCode)

        coVerify(exactly = 1) {
            mockOvp.authenticateVerifier(authRequest = any(), any(), any())
        }
        coVerify(exactly = 1) {
            mockOvp.constructUnsignedVPToken(any(), any(), any())
        }
        coVerify(exactly = 1) {
            mockOvp.constructVPResponse(any(), any())
        }
    }
}
