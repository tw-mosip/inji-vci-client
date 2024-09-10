package io.mosip.vciclient.credentialRequest

import io.mosip.vciclient.constants.CredentialFormat
import io.mosip.vciclient.dto.IssuerMetaData
import io.mosip.vciclient.exception.InvalidDataProvidedException
import io.mosip.vciclient.proof.jwt.JWTProof
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CredentialRequestFactoryTest {
    @Test
    fun `should throw exception when required details are not available in Issuer metadata based on VC format`() {
        assertThrows(
            InvalidDataProvidedException::class.java,
        ) {
            CredentialRequestFactory.createCredentialRequest(
                CredentialFormat.MSO_MDOC, "access-token",
                IssuerMetaData(
                    "/credentialAudience",
                    "https://credentialendpoint/",
                    30000,
                    credentialFormat = CredentialFormat.LDP_VC
                ), JWTProof("headerEncoded.payloadEncoded.signature")
            )
        }
        assertThrows(
            InvalidDataProvidedException::class.java,
        ) {
            CredentialRequestFactory.createCredentialRequest(
                CredentialFormat.MSO_MDOC, "access-token",
                IssuerMetaData(
                    "/credentialAudience",
                    "https://credentialendpoint/",
                    30000,
                    claims = mapOf("org.iso.18013.5.1" to mapOf("given_name" to emptyMap<String, Any>())),
                    credentialFormat = CredentialFormat.MSO_MDOC
                ), JWTProof("headerEncoded.payloadEncoded.signature")
            )
        }

    }
}
