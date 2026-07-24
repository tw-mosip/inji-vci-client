package io.mosip.vciclient.authorizationCodeFlow.interactiveAuthorization.request

import com.google.gson.annotations.SerializedName
import io.mosip.vciclient.common.JsonUtils

internal data class IARInitialRequestBody(
    @SerializedName("response_type")
    val responseType: String = "code",

    @SerializedName("client_id")
    val clientId: String,

    @SerializedName("code_challenge")
    val codeChallenge: String,

    @SerializedName("code_challenge_method")
    val codeChallengeMethod: String = "S256",

    @SerializedName("redirect_uri")
    val redirectUri: String,

    @SerializedName("authorization_details")
    val authorizationDetails: List<AuthorizationDetail>,

    @SerializedName("interaction_types_supported")
    val interactionTypesSupported: List<String>,

    @SerializedName("dpop_jkt")
    val dpopJkt: String? = null
) {
    fun toFormMap(): Map<String, String> = buildMap {
        put("response_type", responseType)
        put("client_id", clientId)
        put("code_challenge", codeChallenge)
        put("code_challenge_method", codeChallengeMethod)
        put("redirect_uri", redirectUri)
        put("authorization_details", JsonUtils.serialize(authorizationDetails))
        put("interaction_types_supported", interactionTypesSupported.joinToString(","))
        dpopJkt?.let { put("dpop_jkt", it) }
    }
}


