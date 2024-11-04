package io.mosip.vciclient.credentialResponse.types.msoMdocVc

import io.mosip.vciclient.common.JsonUtils
import io.mosip.vciclient.credentialResponse.CredentialResponse
import io.mosip.vciclient.credentialResponse.CredentialResponseFactory

class MsoMdocVcCredentialResponseFactory : CredentialResponseFactory {
    override fun constructResponse(response: String): CredentialResponse {
        return JsonUtils.deserialize(response, MsoMdocCredential::class.java)!!
    }
}