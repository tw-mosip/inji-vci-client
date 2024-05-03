package io.mosip.vciclient.dto

import io.mosip.vciclient.constants.CredentialFormat
import org.junit.Assert.assertEquals
import org.junit.Test

class CredentialRequestBodyTest {
    @Test
    fun `should create JSON in expected format`() {
        val credentialRequestBody = CredentialRequestBody(
            credentialDefinition = CredentialDefinition(
                type = arrayOf("VerifiableCredential")
            ),
            proof = Proof(jwt = "header.payload.signature"),
            format = CredentialFormat.LDP_VC.value
        )

        val credentialRequestBodyJson: String = credentialRequestBody.toJson()

        assertEquals(
            "{\"format\":\"ldp_vc\",\"credential_definition\":{\"@context\":[\"https://www.w3.org/2018/credentials/v1\"],\"type\":[\"VerifiableCredential\"]},\"proof\":{\"proof_type\":\"jwt\",\"jwt\":\"header.payload.signature\"}}",
            credentialRequestBodyJson
        )
    }
}