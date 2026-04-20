package io.mosip.vciclient.credential.response

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import io.mosip.vciclient.common.JsonUtils

data class CredentialItem(
    @SerializedName("credential")
    val credential: JsonElement? = null
)

data class CredentialResponse(
    @SerializedName("credentials")
    val credentials: List<CredentialItem>? = null,

    @SerializedName(value = "credential_configuration_id", alternate = ["credentialConfigurationId"])
    var credentialConfigurationId: String? = null,

    @SerializedName(value = "credential_issuer", alternate = ["credentialIssuer"])
    var credentialIssuer: String? = null,
) {
    fun toJsonString(): String {
        return JsonUtils.serialize(this)
    }
}
