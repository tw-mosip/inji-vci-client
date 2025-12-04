package io.mosip.vciclient.authorizationCodeFlow.interactiveAuth

import com.google.gson.annotations.SerializedName

class WebAuthorizationHandler(
    openWebPage: (String) -> AuthorizationResponse
) : AuthorizationHandler {

    override fun type(): String {
        return "redirect_to_web"
    }

    override suspend fun authorizeUser(requestData: AuthorizationRequestData): AuthorizationResponse {
        return AuthorizationResponse(
            error = "not_implemented",
            errorDescription = "WebAuthorizationHandler is not implemented yet"
        )
    }

}

internal data class AuthorizationDetail(
    val type: String,

    @SerializedName("credential_configuration_id")
    val credentialConfigurationId: List<String>,

    val claims: Map<String, Any>? = null
)