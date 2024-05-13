package io.mosip.vciclient.credentialRequest.types

import io.mosip.vciclient.constants.CredentialFormat
import io.mosip.vciclient.proof.jwt.JWTProof
import io.mosip.vciclient.dto.IssuerMetaData
import okhttp3.Request
import okhttp3.internal.http2.Header
import okhttp3.internal.toHeaderList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.URI

class LdpVcCredentialRequestTest {
    @Test
    fun `should create JSON in expected format`() {
        val credentialEndpoint = "https://credentialendpoint/"

        val ldpVcRequest: Request = LdpVcCredentialRequest(
            "accessToken",
            IssuerMetaData(
                "/credentialAudience",
                credentialEndpoint,
                30000,
                credentialType = arrayOf("VerifiableCredential"),
                credentialFormat = CredentialFormat.LDP_VC
            ), JWTProof()
        ).constructRequest()

        assertTrue(
            ldpVcRequest.headers.toHeaderList().containsAll(
                listOf(
                    Header("Authorization", "Bearer accessToken"),
                    Header("Content-Type", "application/json")
                )
            )
        )
        assertEquals(URI(credentialEndpoint), ldpVcRequest.url.toUri())
        assertEquals("POST", ldpVcRequest.method)
    }
}