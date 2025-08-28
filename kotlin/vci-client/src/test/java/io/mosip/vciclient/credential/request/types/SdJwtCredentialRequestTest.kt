package io.mosip.vciclient.credential.request.types

import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mosip.vciclient.constants.CredentialFormat
import io.mosip.vciclient.issuerMetadata.IssuerMetadata
import io.mosip.vciclient.proof.Proof
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SdJwtCredentialRequestTest {

    private val sampleAccessToken = "test-access-token"
    private val sampleCredentialEndpoint = "https://issuer.example.com/credential"
    private val sampleVct = "IdentityCredential"

    private lateinit var sampleProof: Proof
    private lateinit var issuerMetadata: IssuerMetadata

    @Before
    fun setUp() {
        sampleProof = mockk(relaxed = true)

        issuerMetadata = mockk(relaxed = true)
        every { issuerMetadata.credentialEndpoint } returns sampleCredentialEndpoint
        every { issuerMetadata.vct } returns sampleVct
        every { issuerMetadata.credentialFormat } returns CredentialFormat.VC_SD_JWT
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `constructRequest should build a valid POST HTTP request`() {
        val request = SdJwtCredentialRequest(
            accessToken = sampleAccessToken,
            issuerMetadata = issuerMetadata,
            proof = sampleProof
        ).constructRequest()

        assertEquals(sampleCredentialEndpoint, request.url.toString())
        assertEquals("POST", request.method)
        assertEquals("Bearer $sampleAccessToken", request.header("Authorization"))
        assertEquals("application/json", request.header("Content-Type"))
        assertNotNull(request.body)
    }

    @Test
    fun `validateIssuerMetaData should return valid when vct is present`() {
        val validatorResult = SdJwtCredentialRequest(
            accessToken = sampleAccessToken,
            issuerMetadata = issuerMetadata,
            proof = sampleProof
        ).validateIssuerMetaData()

        assertTrue(validatorResult.isValid)
    }

    @Test
    fun `validateIssuerMetaData should return invalid when vct is null`() {
        every { issuerMetadata.vct } returns null

        val validatorResult = SdJwtCredentialRequest(
            accessToken = sampleAccessToken,
            issuerMetadata = issuerMetadata,
            proof = sampleProof
        ).validateIssuerMetaData()

        assertFalse(validatorResult.isValid)
        assertTrue(validatorResult.invalidFields.contains("vct"))
    }

    @Test
    fun `validateIssuerMetaData should return invalid when vct is empty`() {
        every { issuerMetadata.vct } returns ""

        val validatorResult = SdJwtCredentialRequest(
            accessToken = sampleAccessToken,
            issuerMetadata = issuerMetadata,
            proof = sampleProof
        ).validateIssuerMetaData()

        assertFalse(validatorResult.isValid)
        assertTrue(validatorResult.invalidFields.contains("vct"))
    }

    @Test
    fun `constructRequest should include claims and proof in body JSON`() {
        val request = SdJwtCredentialRequest(
            accessToken = sampleAccessToken,
            issuerMetadata = issuerMetadata,
            proof = sampleProof
        ).constructRequest()

        val buffer = okio.Buffer()
        request.body!!.writeTo(buffer)
        val requestBodyString = buffer.readUtf8()

        assertTrue(requestBodyString.contains("IdentityCredential"))
        assertTrue(requestBodyString.contains("proof"))
    }
}
