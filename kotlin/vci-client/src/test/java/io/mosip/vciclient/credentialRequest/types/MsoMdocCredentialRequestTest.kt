package io.mosip.vciclient.credentialRequest.types

import io.mosip.vciclient.constants.CredentialFormat
import io.mosip.vciclient.credentialRequest.util.ValidatorResult
import io.mosip.vciclient.dto.IssuerMetaData
import io.mosip.vciclient.proof.jwt.JWTProof
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.internal.http2.Header
import okhttp3.internal.toHeaderList
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.URI


class MsoMdocCredentialRequestTest {
    @Test
    fun `should create JSON in expected format`() {
        val credentialEndpoint = "https://credentialendpoint/"

        val msoMdocCredentialRequest: Request = MsoMdocCredentialRequest(
            "accessToken",
            IssuerMetaData(
                "/credentialAudience",
                credentialEndpoint,
                30000,
                docType = "org.iso.18013.5.1.mDL",
                claims = mapOf("org.iso.18013.5.1" to mapOf("given_name" to emptyMap<String, Any>())),
                credentialFormat = CredentialFormat.MSO_MDOC
            ), JWTProof("headerEncoded.payloadEncoded.signature")
        ).constructRequest()

        assertTrue(
            msoMdocCredentialRequest.headers.toHeaderList().containsAll(
                listOf(
                    Header("Authorization", "Bearer accessToken"),
                    Header("Content-Type", "application/json")
                )
            )
        )
        assertEquals(URI(credentialEndpoint), msoMdocCredentialRequest.url.toUri())
        assertEquals("POST", msoMdocCredentialRequest.method)
        assertEquals(
            "{\"format\":\"mso_mdoc\",\"doctype\":\"org.iso.18013.5.1.mDL\",\"claims\":{\"org.iso.18013.5.1\":{\"given_name\":{}}},\"proof\":{\"proof_type\":\"jwt\",\"jwt\":\"headerEncoded.payloadEncoded.signature\"}}",
            getRequestBodyInJsonString(msoMdocCredentialRequest.body!!)
        )
    }

    @Test
    fun `should return isValidated as true when required issuerMetadata details are available`() {
        val msoMdocCredentialRequest = MsoMdocCredentialRequest(
            "accessToken",
            IssuerMetaData(
                "/credentialAudience",
                "https://credentialendpoint/",
                30000,
                docType = "org.iso.18013.5.1.mDL",
                claims = mapOf("org.iso.18013.5.1" to mapOf("given_name" to emptyMap<String, Any>())),
                credentialFormat = CredentialFormat.MSO_MDOC
            ), JWTProof("headerEncoded.payloadEncoded.signature")
        )

        val validatorResult: ValidatorResult = msoMdocCredentialRequest.validateIssuerMetaData()

        assertTrue(validatorResult.isValidated)
        assertTrue(validatorResult.invalidFields.isEmpty())
    }

    @Test
    fun `should return validator result with isValidated as false & invalidFields when required issuerMetadata details are not available`() {
        val validatorResult1: ValidatorResult = MsoMdocCredentialRequest(
            "accessToken",
            IssuerMetaData(
                "/credentialAudience",
                "https://credentialendpoint/",
                30000,
                claims = mapOf<String, Map<String, Map<String, Any>>>("org.iso.18013.5.1" to mapOf("given_name" to emptyMap())),
                credentialFormat = CredentialFormat.MSO_MDOC
            ), JWTProof("headerEncoded.payloadEncoded.signature")
        ).validateIssuerMetaData()
        val validatorResult2: ValidatorResult = MsoMdocCredentialRequest(
            "accessToken",
            IssuerMetaData(
                "/credentialAudience",
                "https://credentialendpoint/",
                30000,
                docType = "org.iso.18013.5.1.mDL",
                credentialFormat = CredentialFormat.MSO_MDOC
            ), JWTProof("headerEncoded.payloadEncoded.signature")
        ).validateIssuerMetaData()
        val validatorResult3: ValidatorResult = MsoMdocCredentialRequest(
            "accessToken",
            IssuerMetaData(
                "/credentialAudience",
                "https://credentialendpoint/",
                30000,
                credentialFormat = CredentialFormat.MSO_MDOC
            ), JWTProof("headerEncoded.payloadEncoded.signature")
        ).validateIssuerMetaData()

        assertFalse(validatorResult1.isValidated)
        assertFalse(validatorResult2.isValidated)
        assertFalse(validatorResult3.isValidated)
        assertEquals(listOf("docType"), validatorResult1.invalidFields)
        assertEquals(listOf("claims"), validatorResult2.invalidFields)
        assertEquals(listOf("docType", "claims"), validatorResult3.invalidFields)
    }


    private fun getRequestBodyInJsonString(requestBody: RequestBody): String {
        val buffer = Buffer()
        requestBody.writeTo(buffer)
        return buffer.readUtf8()
    }
}