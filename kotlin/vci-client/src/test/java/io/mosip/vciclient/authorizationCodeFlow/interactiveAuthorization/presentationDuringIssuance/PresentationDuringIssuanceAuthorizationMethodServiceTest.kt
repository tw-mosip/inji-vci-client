package io.mosip.vciclient.authorizationCodeFlow.interactiveAuthorization.presentationDuringIssuance

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import io.mosip.openID4VP.OpenID4VP
import io.mosip.openID4VP.authorizationRequest.AuthorizationRequest
import io.mosip.openID4VP.authorizationRequest.presentationDefinition.PresentationDefinition
import io.mosip.openID4VP.authorizationResponse.unsignedVPToken.types.ldp.UnsignedLdpVPToken
import io.mosip.openID4VP.authorizationResponse.vpTokenSigningResult.VPTokenSigningResultV2
import io.mosip.openID4VP.authorizationResponse.vpTokenSigningResult.types.ldp.LdpVPTokenSigningResult
import io.mosip.openID4VP.constants.FormatType
import io.mosip.vciclient.authorizationCodeFlow.interactiveAuthorization.request.AuthorizationRequestData
import io.mosip.vciclient.exception.InteractiveAuthorizationException
import io.mosip.vciclient.networkManager.NetworkManager
import io.mosip.vciclient.networkManager.NetworkResponse
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertThrows

class PresentationDuringIssuanceAuthorizationMethodServiceTest {

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

        // ✅ IMPORTANT: mock positionally (avoid overload + named args mismatch)
        coEvery {
            mockOvp.authenticateVerifier(
                authorizationRequest = any(),
                any(),
                any(),
            )
        } returns fakeAuthRequest

        every { NetworkManager.sendRequest(any(), any(), any(), any()) } returns
                NetworkResponse("""{"status":"error"}""", null)
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

    // ------------------------------------------------------------------------

    @Test
    fun `should throw when request type is invalid`() = runTest {
        val handler = PresentationDuringIssuanceAuthorizationMethodService(
            selectCredentialsForPresentation = { emptyMap() },
            signVerifiablePresentation = { emptyList() },
            traceabilityId = "test-trace-id",
        )

        assertThrows<InteractiveAuthorizationException> {
            handler.authorizeUser(AuthorizationRequestData())
        }
    }

    @Test
    fun `should successfully authorize and return success response`() = runTest {
        coEvery { mockOvp.constructUnsignedVPToken(any(), any(), any()) } returns mapOf(
            FormatType.LDP_VC to UnsignedLdpVPToken("unsigned")
        )

        coEvery { mockOvp.constructVPResponse(any()) } returns mapOf("vp_token" to "signed")

        every { NetworkManager.sendRequest(any(), any(), any(), any()) } returns NetworkResponse(
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
                listOf(
                    VPTokenSigningResultV2(
                        signedData = "signed",
                    )
                )
            },
            ldpVpSignatureSuite = "Ed25519Signature2020",
            openId4vp = mockOvp,
            traceabilityId = "test-trace-id"
        )

        val result = handler.authorizeUser(validRequest())

        Assert.assertEquals("success", result.status)
        Assert.assertEquals("auth-code-123", result.authorizationCode)

        coVerify(exactly = 1) {
            mockOvp.authenticateVerifier(
                authorizationRequest = any(),
                any(),
                any()
            )
        }
        coVerify(exactly = 1) { mockOvp.constructUnsignedVPTokenV2(any(), any(), any()) }
        coVerify(exactly = 1) { mockOvp.constructVPResponseV2(any()) }
    }

    @Test
    fun `should post VP error response when user selects no credentials`() = runTest {

        val handler = PresentationDuringIssuanceAuthorizationMethodService(
            selectCredentialsForPresentation = { emptyMap() },
            signVerifiablePresentation = { emptyList() },
            openId4vp = mockOvp,
            traceabilityId = "test-trace-id"
        )

        handler.authorizeUser(validRequest())

        verify(exactly = 1) { NetworkManager.sendRequest(any(), any(), any(), any()) }

        coVerify(exactly = 0) { mockOvp.constructUnsignedVPToken(any(), any(), any()) }
        coVerify(exactly = 0) { mockOvp.constructVPResponse(any()) }
    }

    @Test
    fun `should post VP error response when signatureSuite missing for LDP VC`() = runTest {
        val handler = PresentationDuringIssuanceAuthorizationMethodService(
            selectCredentialsForPresentation = { validCredentialMap() }, // contains LDP_VC
            signVerifiablePresentation = { emptyList() },
            ldpVpSignatureSuite = null, // triggers InteractiveAuthorizationException in handlePresentation
            openId4vp = mockOvp,
            traceabilityId = "test-trace-id"
        )

        handler.authorizeUser(validRequest())

        verify(exactly = 1) { NetworkManager.sendRequest(any(), any(), any(), any()) }

        coVerify(exactly = 0) { mockOvp.constructUnsignedVPToken(any(), any(), any()) }
    }

    @Test
    fun `should throw when network post fails`() = runTest {
        every {
            NetworkManager.sendRequest(
                any(),
                any(),
                any(),
                any()
            )
        } throws RuntimeException("boom")

        val handler = PresentationDuringIssuanceAuthorizationMethodService(
            selectCredentialsForPresentation = { validCredentialMap() },
            signVerifiablePresentation = { emptyList() },
            ldpVpSignatureSuite = "Ed25519Signature2020",
            openId4vp = mockOvp,
            traceabilityId = "test-trace-id"
        )

        assertThrows<InteractiveAuthorizationException> {
            handler.authorizeUser(validRequest())
        }
    }

    @Test
    fun `should throw when issuer response deserialization fails`() = runTest {
        every { NetworkManager.sendRequest(any(), any(), any(), any()) } returns
                NetworkResponse(""""not":"a-valid-AuthorizationResponse" }""", null)

        val handler = PresentationDuringIssuanceAuthorizationMethodService(
            selectCredentialsForPresentation = { validCredentialMap() },
            signVerifiablePresentation = { emptyList() },
            ldpVpSignatureSuite = "Ed25519Signature2020",
            openId4vp = mockOvp,
            traceabilityId = "test-trace-id"
        )

        assertThrows<InteractiveAuthorizationException> {
            handler.authorizeUser(validRequest())
        }
    }
}
