package io.mosip.vciclient.authorizationCodeFlow.interactiveAuth.redirectToWeb

import io.mosip.vciclient.authorizationCodeFlow.clientMetadata.ClientMetadata
import io.mosip.vciclient.authorizationCodeFlow.interactiveAuth.request.AuthorizationRequestData
import io.mosip.vciclient.pkce.PKCESessionManager

data class StandardAuthorizationRequestData(
    val authorizeUrl: String,
    val clientMetadata: ClientMetadata,
    val pkceSession: PKCESessionManager.PKCESession,
    val scope: String,
) : AuthorizationRequestData()
