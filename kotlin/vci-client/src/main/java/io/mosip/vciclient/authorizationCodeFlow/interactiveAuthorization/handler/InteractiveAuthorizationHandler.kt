package io.mosip.vciclient.authorizationCodeFlow.interactiveAuthorization.handler

import AuthorizationDetail
import io.mosip.vciclient.authorizationCodeFlow.AuthorizationMethod
import io.mosip.vciclient.authorizationCodeFlow.clientMetadata.ClientMetadata
import io.mosip.vciclient.authorizationCodeFlow.interactiveAuthorization.presentationDuringIssuance.OpenId4VpPresentationResponse
import io.mosip.vciclient.authorizationCodeFlow.interactiveAuthorization.presentationDuringIssuance.OpenId4VpPresentationAuthorizationRequestData
import io.mosip.vciclient.authorizationCodeFlow.interactiveAuthorization.presentationDuringIssuance.PresentationDuringIssuanceAuthorizationMethodService
import io.mosip.vciclient.authorizationCodeFlow.interactiveAuthorization.request.IARInitialRequestBody
import io.mosip.vciclient.authorizationCodeFlow.interactiveAuthorization.response.AuthorizationResponse
import io.mosip.vciclient.common.JsonUtils
import io.mosip.vciclient.exception.InteractiveAuthorizationException
import io.mosip.vciclient.networkManager.HttpMethod
import io.mosip.vciclient.networkManager.NetworkManager
import io.mosip.vciclient.pkce.PKCESessionManager
import org.json.JSONObject
import java.util.logging.Logger

class InteractiveAuthorizationHandler {

    private val logger = Logger.getLogger(javaClass.simpleName)

    suspend fun handle(
        endpoint: String,
        clientMetadata: ClientMetadata,
        credentialConfigurationId: String,
        authorizationMethods: List<AuthorizationMethod>,
        pkceSession: PKCESessionManager.PKCESession
    ): AuthorizationResponse {

        return try {
            val interactionTypesSupported = authorizationMethods.map { it.type.value }
            val requestMap = buildIarRequest(
                clientMetadata,
                credentialConfigurationId,
                pkceSession,
                interactionTypesSupported
            )

            val response = NetworkManager.sendRequest(
                url = endpoint,
                method = HttpMethod.POST,
                bodyParams = requestMap,
                headers = mapOf("Content-Type" to "application/x-www-form-urlencoded")
            )

            val type = try {
                extractInteractionType(response.body)
            } catch (
                e: Exception
            ) {
                throw InteractiveAuthorizationException("Failed to parse and extract interaction type: ${e.message}")
            }


            when (type) {
                InteractionType.OpenId4VpPresentation.value ->
                    handlePresentationInteraction(response.body, authorizationMethods, endpoint)

                else ->
                    throw InteractiveAuthorizationException("Unsupported interaction type: $type")
            }

        } catch (e: InteractiveAuthorizationException) {
            logger.warning("IAR Error: ${e.message}")
            throw e
        } catch (e: Exception) {
            logger.severe("IAR Fatal Error: ${e.message}")
            throw InteractiveAuthorizationException("Interactive authorization failed: ${e.message}")
        }
    }

    private fun buildIarRequest(
        clientMetadata: ClientMetadata,
        credentialConfigId: String,
        pkce: PKCESessionManager.PKCESession,
        interactionTypesSupported: List<String>
    ): Map<String, String> {
        val details = listOf(
            AuthorizationDetail(
                type = "openid_credential",
                credentialConfigurationId = listOf(credentialConfigId)
            )
        )

        return IARInitialRequestBody(
            clientId = clientMetadata.clientId,
            codeChallenge = pkce.codeChallenge,
            redirectUri = clientMetadata.redirectUri,
            authorizationDetails = details,
            interactionTypesSupported = interactionTypesSupported
        ).toFormMap()
    }

    private fun extractInteractionType(responseBody: String): String =
        JSONObject(responseBody).optString("type", "")


    private suspend fun handlePresentationInteraction(
        responseBody: String,
        authorizationMethods: List<AuthorizationMethod>,
        endpoint: String
    ): AuthorizationResponse {

        val parsed = JsonUtils.deserialize(responseBody, OpenId4VpPresentationResponse::class.java)
            ?: throw InteractiveAuthorizationException("Failed to parse OpenID4VP response")

        try {
            parsed.validate()
        } catch (e: Exception) {
            throw InteractiveAuthorizationException("Invalid OpenID4VP response: ${e.message}")
        }

        val presentationMethod = authorizationMethods
            .filterIsInstance<AuthorizationMethod.PresentationDuringIssuance>()
            .firstOrNull()
            ?: throw InteractiveAuthorizationException("Presentation callback missing")

        val request = OpenId4VpPresentationAuthorizationRequestData(
            ovpRequest = parsed.openid4vpRequest,
            authSession = parsed.authSession,
            iar = endpoint
        )

        val handler = PresentationDuringIssuanceAuthorizationMethodService(
            handlePresentationRequest = presentationMethod.selectCredentialsForPresentation,
            signVerifiablePresentation = presentationMethod.signVerifiablePresentation
        )

        return handler.authorizeUser(request)
    }
}
