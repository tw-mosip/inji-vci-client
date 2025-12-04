package io.mosip.vciclient.authorizationCodeFlow.interactiveAuth

data class StandardAuthorizationRequestData(
    val authorizeUrl: String,
    val clientConfig: ClientConfig,
) : AuthorizationRequestData()
