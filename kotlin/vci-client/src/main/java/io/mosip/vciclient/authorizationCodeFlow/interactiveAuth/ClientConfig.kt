package io.mosip.vciclient.authorizationCodeFlow.interactiveAuth

import io.mosip.vciclient.authorizationCodeFlow.clientMetadata.ClientMetadata
import io.mosip.vciclient.pkce.PKCESessionManager

data class ClientConfig(
    val clientMetadata: ClientMetadata,
    val pkceSession: PKCESessionManager.PKCESession,
    val codeChallenge: String? = null,
    val codeChallengeMethod: String? = null
)
