package io.mosip.vciclient.credentialResponse

import io.mosip.vciclient.constants.CredentialFormat
import io.mosip.vciclient.credentialResponse.types.ldpVc.LdpVcCredentialResponseFactory
import io.mosip.vciclient.credentialResponse.types.ldpVc.MdocVcCredentialResponseFactory

interface CredentialResponseFactory {
    companion object {
        fun createCredentialResponse(
            formatType: CredentialFormat,
            response: String,
        ): CredentialResponse {
            when (formatType) {
                CredentialFormat.LDP_VC -> return LdpVcCredentialResponseFactory().constructResponse(
                    response
                )

                CredentialFormat.MSO_MDOC -> return MdocVcCredentialResponseFactory().constructResponse(
                    response
                )
            }
        }
    }

    fun constructResponse(response: String): CredentialResponse
}