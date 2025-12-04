package io.mosip.vciclient.authorizationCodeFlow.interactiveAuth

data class AuthorizationResponse(
    val authorizationCode: String? = null,
    val status: String? = null,
    val error: String? = null,
    val errorDescription: String? = null,
    val authSession: String? = null
)