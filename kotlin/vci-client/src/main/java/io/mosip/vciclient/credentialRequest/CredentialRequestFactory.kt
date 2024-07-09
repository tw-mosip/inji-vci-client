package io.mosip.vciclient.credentialRequest

import io.mosip.vciclient.constants.CredentialFormat
import io.mosip.vciclient.proof.Proof
import io.mosip.vciclient.credentialRequest.types.LdpVcCredentialRequest
import io.mosip.vciclient.dto.IssuerMetaData
import okhttp3.Request

class CredentialRequestFactory {
    companion object {
        fun createCredentialRequest(
            credentialFormat: CredentialFormat,
            accessToken: String,
            issuerMetaData: IssuerMetaData,
            proof: Proof,
        ): Request {
            when (credentialFormat) {
                CredentialFormat.LDP_VC -> return LdpVcCredentialRequest(
                    accessToken,
                    issuerMetaData,
                    proof
                ).constructRequest()

                CredentialFormat.MSO_MDOC -> return LdpVcCredentialRequest(
                    accessToken,
                    issuerMetaData,
                    proof
                ).constructRequest()
            }
        }
    }
}