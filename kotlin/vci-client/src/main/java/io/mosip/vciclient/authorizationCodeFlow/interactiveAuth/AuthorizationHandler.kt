package io.mosip.vciclient.authorizationCodeFlow.interactiveAuth

interface AuthorizationHandler {
    fun type(): String
    suspend fun authorizeUser(requestData: AuthorizationRequestData): AuthorizationResponse
}