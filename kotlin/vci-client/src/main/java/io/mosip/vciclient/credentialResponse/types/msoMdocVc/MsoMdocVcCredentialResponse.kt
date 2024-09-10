package io.mosip.vciclient.credentialResponse.types.msoMdocVc


import io.mosip.vciclient.credentialResponse.CredentialResponse
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

data class MsoMdocCredential(
    val credential: JsonObject,
) : CredentialResponse {
    override fun toJsonString(): String {
        return buildJsonObject {
            put("credential", credential)
        }.toString()
    }
}