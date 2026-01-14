package io.mosip.vciclient.authorizationCodeFlow.interactiveAuthorization.request

import com.google.gson.annotations.SerializedName

data class AuthorizationDetail(
    val type: String,

    @SerializedName("credential_configuration_id")
    val credentialConfigurationId: String,

    val claims: Map<String, Any>? = null
)