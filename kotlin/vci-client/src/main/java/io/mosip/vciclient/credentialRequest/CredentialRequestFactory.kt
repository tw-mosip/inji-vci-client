package io.mosip.vciclient.credentialRequest

import io.mosip.vciclient.constants.CredentialFormat
import io.mosip.vciclient.credentialRequest.types.LdpVcCredentialRequest
import io.mosip.vciclient.dto.IssuerMeta
import okhttp3.Request

class CredentialRequestFactory {
    companion object {
        fun createCredentialRequest(
            credentialFormat: CredentialFormat,
            accessToken: String,
            issuerMeta: IssuerMeta,
            proofJWT: String,
        ): Request? {
            when (credentialFormat) {
                CredentialFormat.LDP_VC -> return LdpVcCredentialRequest(
                    accessToken,
                    issuerMeta,
                    proofJWT
                ).constructRequest()
            }
            return null
        }
    }
}