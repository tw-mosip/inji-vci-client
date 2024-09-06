package io.mosip.vciclient.credentialResponse.types.mdocVc


import io.mosip.vciclient.credentialResponse.CredentialResponse
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

data class MdocCredential(
    val credential: JsonObject,
) : CredentialResponse {
    override fun toJsonString(): String {
        return buildJsonObject {
            put("credential", credential)
        }.toString()
    }
}