package io.mosip.vciclient.authorizationCodeFlow.interactiveAuth.presentationDuringIssuance

import io.mosip.vciclient.authorizationCodeFlow.interactiveAuth.request.AuthorizationRequestData

data class PresentationAuthorizationRequestData(
    val ovpRequest: Map<String, Any>,
    val authSession: String?,
    val iar: String
) : AuthorizationRequestData()
