package io.mosip.vciclient.credentialResponse.types.ldpVc

import com.google.gson.Gson
import io.mosip.vciclient.credentialResponse.CredentialResponse
import io.mosip.vciclient.credentialResponse.CredentialResponseFactory

class LdpVcCredentialResponseFactory : CredentialResponseFactory {
    override fun constructResponse(response: String): CredentialResponse {
        return Gson().fromJson(response, LdpVcCredentialResponse::class.java)
    }
}