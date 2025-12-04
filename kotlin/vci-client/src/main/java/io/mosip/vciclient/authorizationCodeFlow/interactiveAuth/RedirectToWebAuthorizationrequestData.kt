package io.mosip.vciclient.authorizationCodeFlow.interactiveAuth

import io.mosip.vciclient.authorizationCodeFlow.clientMetadata.ClientMetadata
import io.mosip.vciclient.pkce.PKCESessionManager

data class RedirectToWebAuthorizationRequestData(
    val requestUri: String,
    val expiresIn: Long,
    val authSession: String,
    val authorizeUrl: String,
    val clientConfig: ClientConfig,
) : AuthorizationRequestData()
