package io.mosip.vciclient.credentialResponse.types.msoMdocVc

import io.mosip.vciclient.common.JsonUtils
import io.mosip.vciclient.credentialResponse.CredentialResponse

data class MsoMdocCredential(
    val credential: String,
) : CredentialResponse {
    override fun toJsonString(): String {
        return JsonUtils.serialize(this)
    }
}