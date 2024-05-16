package io.mosip.vciclient.credentialResponse.types.ldpVc

import io.mosip.vciclient.common.JsonUtils
import io.mosip.vciclient.credentialResponse.CredentialResponse
import io.mosip.vciclient.credentialResponse.CredentialResponseFactory

class LdpVcCredentialResponseFactory : CredentialResponseFactory {
    override fun constructResponse(response: String): CredentialResponse {
        return JsonUtils.deserialize(response, LdpVcCredentialResponse::class.java)!!
    }
}