package io.mosip.vciclient.authorizationCodeFlow.interactiveAuth.handler

import io.mosip.vciclient.authorizationCodeFlow.interactiveAuth.request.AuthorizationRequestData
import io.mosip.vciclient.authorizationCodeFlow.interactiveAuth.response.AuthorizationResponse

interface AuthorizationHandler {
    fun type(): String
    suspend fun authorizeUser(requestData: AuthorizationRequestData): AuthorizationResponse
}