package io.mosip.vciclient.credential.response

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import io.mosip.vciclient.common.JsonUtils

data class CredentialResponseDraft13(
    val credential: JsonElement,

    @SerializedName(value = "credentialConfigurationId")
    var credentialConfigurationId: String? = null,

    @SerializedName(value = "credentialIssuer")
    var credentialIssuer: String? = null,
) {
    fun toJsonString(): String {
        return JsonUtils.serialize(this)
    }
}
