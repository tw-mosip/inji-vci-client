package io.mosip.vciclient.authorizationCodeFlow.interactiveAuth

import io.mosip.vciclient.authorizationCodeFlow.clientMetadata.ClientMetadata
import io.mosip.vciclient.common.JsonUtils
import io.mosip.vciclient.networkManager.HttpMethod
import io.mosip.vciclient.networkManager.NetworkManager
import io.mosip.vciclient.pkce.PKCESessionManager
import org.json.JSONObject

class InteractiveAuthorizationHandler {
    suspend fun handle(
        endpoint: String,
        clientMetadata: ClientMetadata,
        credentialConfigurationId: String,
        interactionTypesSupported: List<String>,
        callback: List<AuthorizationHandler>,
        pkceSession: PKCESessionManager.PKCESession
    ): AuthorizationResponse {
        val authorizationDetailsList = listOf(
            AuthorizationDetail(
                type = "openid_credential",
                credentialConfigurationId = listOf(credentialConfigurationId)
            )
        )

        val iarRequestBody = IARInitialRequestBody(
            clientId = clientMetadata.clientId,
            codeChallenge = pkceSession.codeChallenge,
            redirectUri = clientMetadata.redirectUri,
            authorizationDetails = authorizationDetailsList,
            interactionTypesSupported = interactionTypesSupported
        ).toFormMap()

        val response = NetworkManager.sendRequest(
            url = endpoint,
            method = HttpMethod.POST,
            bodyParams = iarRequestBody,
            headers = mapOf("Content-Type" to "application/json")
        )

        val jsonResponse = JSONObject(response.body)

        if (jsonResponse["type"] == "openid4vp_presentation") {
            val openIdVpInteractionResponse = JsonUtils.deserialize(
                response.body, OpenId4VpPresentationResponse::class.java
            )
            openIdVpInteractionResponse?.validate()
            val presentationAuthorizationRequest = PresentationAuthorizationRequestData(
                ovpRequest = openIdVpInteractionResponse?.openid4vpRequest
                    ?: throw IllegalStateException("openid4vp_request is missing in the response"),
                authSession = openIdVpInteractionResponse.authSession,
                iar = endpoint
            )
            return callback.find { callback -> callback.type() == "openid4vp_presentation" }
                ?.authorizeUser(presentationAuthorizationRequest)
                ?: throw IllegalStateException("No matching callback found for openid4vp_presentation")
        } else {
            throw IllegalStateException("Unsupported interaction type: ${jsonResponse["type"]}")
        }
    }
}