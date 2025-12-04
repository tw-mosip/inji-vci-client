package io.mosip.vciclient.authorizationCodeFlow.interactiveAuth

data class PresentationAuthorizationRequestData(
    val ovpRequest: Map<String, Any>,
    val authSession: String?,
    val iar: String
) : AuthorizationRequestData()
