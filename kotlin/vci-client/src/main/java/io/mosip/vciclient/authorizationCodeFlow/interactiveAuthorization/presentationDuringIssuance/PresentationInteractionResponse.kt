package io.mosip.vciclient.authorizationCodeFlow.interactiveAuthorization.presentationDuringIssuance

import com.google.gson.annotations.SerializedName
import io.mosip.vciclient.authorizationCodeFlow.interactiveAuthorization.response.AuthorizationResponse

data class PresentationInteractionResponse(
    @SerializedName("status")
    override val status: String,
    @SerializedName("type")
    override val type: String,
    @SerializedName("auth_session")
    override val authSession: String,
    @SerializedName("openid4vp_request")
    val openid4vpRequest: Map<String, Any>
) : AuthorizationResponse(status, type, authSession) {

    override fun validate() {

        if (type != "openid4vp_presentation") {
            throw IllegalArgumentException("Invalid type: expected 'openid4vp_presentation'")
        }

        if (openid4vpRequest.isEmpty()) {
            throw IllegalArgumentException("openid4vpRequest must not be empty")
        }

        if (openid4vpRequest.containsKey("request")) {
            validateSignedRequest()
        } else {
            validateUnsignedRequest()
        }
    }

    private fun validateUnsignedRequest() {
        val responseType = openid4vpRequest["response_type"] as? String
        if (responseType != "vp_token") {
            throw IllegalArgumentException("response_type must be 'vp_token'")
        }

        val responseMode = openid4vpRequest["response_mode"] as? String
            ?: throw IllegalArgumentException("Missing or invalid 'response_mode'")
        if (responseMode !in listOf("iar-post", "iar-post.jwt")) {
            throw IllegalArgumentException("response_mode must be 'iar-post' or 'iar-post.jwt'")
        }
    }

    private fun validateSignedRequest() {
        openid4vpRequest["request"] as? String
            ?: throw IllegalArgumentException("Missing or invalid 'request' JWT")
    }

}