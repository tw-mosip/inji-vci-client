package io.mosip.vciclient.authorizationCodeFlow.interactiveAuthorization.presentationDuringIssuance

import io.mosip.vciclient.authorizationCodeFlow.interactiveAuthorization.request.AuthorizationRequestData

data class PresentationDuringIssuanceRequestData(
    val ovpRequest: Map<String, Any>,
    val authSession: String,
    val iar: String
) : AuthorizationRequestData()
