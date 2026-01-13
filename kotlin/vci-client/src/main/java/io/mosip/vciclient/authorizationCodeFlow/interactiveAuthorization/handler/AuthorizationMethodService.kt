package io.mosip.vciclient.authorizationCodeFlow.interactiveAuthorization.handler

import io.mosip.vciclient.authorizationCodeFlow.interactiveAuthorization.request.AuthorizationRequestData
import io.mosip.vciclient.authorizationCodeFlow.interactiveAuthorization.response.AuthorizationResponse

interface AuthorizationMethodService {
    fun type(): String
    suspend fun authorizeUser(requestData: AuthorizationRequestData): AuthorizationResponse
}