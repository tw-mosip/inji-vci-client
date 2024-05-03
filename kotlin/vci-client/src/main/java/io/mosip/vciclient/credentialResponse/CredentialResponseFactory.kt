package io.mosip.vciclient.credentialResponse

import io.mosip.vciclient.constants.CredentialFormat
import io.mosip.vciclient.credentialResponse.types.ldpVc.LdpVcCredentialResponseFactory

interface CredentialResponseFactory {
    companion object {
        fun createCredentialResponse(formatType: CredentialFormat, response: String): CredentialResponse? {
            when (formatType) {
                CredentialFormat.LDP_VC -> return LdpVcCredentialResponseFactory().constructResponse(
                    response
                )
            }
            return null
        }
    }

    fun constructResponse(response: String): CredentialResponse
}