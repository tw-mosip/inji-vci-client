package io.mosip.vciclient.authorizationCodeFlow.interactiveAuthorization.presentationDuringIssuance

import io.mosip.openID4VP.OpenID4VP
import io.mosip.openID4VP.authorizationRequest.AuthorizationRequest
import io.mosip.openID4VP.authorizationResponse.unsignedVPToken.UnsignedVPToken
import io.mosip.openID4VP.authorizationResponse.vpTokenSigningResult.VPTokenSigningResult
import io.mosip.openID4VP.constants.FormatType
import io.mosip.openID4VP.exceptions.OpenID4VPExceptions
import io.mosip.vciclient.authorizationCodeFlow.interactiveAuthorization.handler.AuthorizationMethodService
import io.mosip.vciclient.authorizationCodeFlow.interactiveAuthorization.handler.InteractionType
import io.mosip.vciclient.authorizationCodeFlow.interactiveAuthorization.request.AuthorizationRequestData
import io.mosip.vciclient.authorizationCodeFlow.interactiveAuthorization.response.InteractionResponse
import io.mosip.vciclient.common.JsonUtils
import io.mosip.vciclient.common.Util
import io.mosip.vciclient.constants.Constants.APPLICATION_X_WWW_FORM_URLENCODED
import io.mosip.vciclient.constants.Constants.CONTENT_TYPE
import io.mosip.vciclient.exception.InteractiveAuthorizationException
import io.mosip.vciclient.networkManager.HttpMethod
import io.mosip.vciclient.networkManager.NetworkManager
import io.mosip.vercred.vcverifier.keyResolver.types.did.DidPublicKeyResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.PublicKey
import java.util.logging.Logger

class PresentationDuringIssuanceAuthorizationMethodService(
    private val selectCredentialsForPresentation: suspend (ovpRequest: AuthorizationRequest) -> Map<String, Map<FormatType, List<Any>>>,
    private val signVerifiablePresentation: suspend (
        payload: Map<FormatType, UnsignedVPToken>,
    ) -> Map<FormatType, VPTokenSigningResult>,
    private val traceabilityId: String? = null,
    private val openId4vp: OpenID4VP = OpenID4VP(
        traceabilityId = traceabilityId ?: "",
        walletMetadata = null
    ),
    private val didPublicKeyResolver: (uri: String) -> PublicKey = { uri ->
        DidPublicKeyResolver().resolve(uri)
    },
) : AuthorizationMethodService {

    private val logTag = Util.getLogTag(javaClass.simpleName, traceabilityId)
    private val logger = Logger.getLogger(logTag)

    override fun type(): String = InteractionType.OpenId4VpPresentation.value

    override suspend fun authorizeUser(
        requestData: AuthorizationRequestData
    ): InteractionResponse {

        if (requestData !is PresentationDuringIssuanceRequestData) {
            throw InteractiveAuthorizationException(
                "Expected OpenId4VpPresentationAuthorizationRequestData"
            )
        }

        var vpResponse: Map<String, Any>

        try {
            val authorizationRequest =
                validatePresentationRequest(requestData.ovpRequest)

            vpResponse = handlePresentation(authorizationRequest)

        } catch (error: Exception) {
            logger.warning("Error during presentation handling: ${error.message}")
            vpResponse = openId4vp.constructErrorInfo(error)
        }

        return try {
            sendOVPAuthorizationResponseToIssuer(
                iar = requestData.iar,
                authSession = requestData.authSession,
                vpResponse = vpResponse
            )
        } catch (ex: Exception) {
            throw InteractiveAuthorizationException(
                "Failed to send VP response to issuer. ${ex.message}"
            )
        }
    }


    private fun validatePresentationRequest(request: Map<String, Any>): AuthorizationRequest {
        return openId4vp.authenticateVerifier(request, emptyList(), false)
    }

    private suspend fun handlePresentation(request: AuthorizationRequest): Map<String, Any> {
        val credentialsMap =
            selectCredentialsForPresentation(request)

        if (credentialsMap.isEmpty()) {
            throw OpenID4VPExceptions.AccessDenied(
                "No credentials selected by user",
                className = "PresentationDuringIssuanceAuthorizationMethodService",
            )
        }

        val firstLdpCredential = findFirstLdpCredential(credentialsMap)
        val holderId = extractHolderIdForLdpVc(firstLdpCredential)
        val signatureSuite = resolveSignatureSuite(holderId)

        val unsignedVpTokens =
            openId4vp.constructUnsignedVPToken(
                verifiableCredentials = credentialsMap,
                holderId = holderId,
                signatureSuite = signatureSuite
            )


        val signedVpTokens = try {
            signVerifiablePresentation(unsignedVpTokens)
        } catch (ex: Exception) {
            throw InteractiveAuthorizationException("Failed to sign VP token. ${ex.message}")
        }


        return openId4vp.constructVPResponse(
            vpTokenSigningResults = signedVpTokens
        )

    }

    private fun findFirstLdpCredential(
        credentialsMap: Map<String, Map<FormatType, List<Any>>>
    ): Any? {
        return credentialsMap
            .values.firstNotNullOfOrNull { it[FormatType.LDP_VC] }
            ?.firstOrNull()
    }

    private fun extractHolderIdForLdpVc(credential: Any?): String? {
        if (credential == null) return null

        val vc = when (credential) {
            is String -> JsonUtils.deserialize(credential, Map::class.java)
            is Map<*, *> -> credential
            else -> null
        }

        val credentialSubject = vc?.get("credentialSubject") as? Map<*, *>

        val holderId = credentialSubject?.get("id") as? String

        return holderId?.let { it.trimEnd('=') + "#0"}
    }

    private fun resolveSignatureSuite(holderId: String?): String? {
        if (holderId == null) return null
        return resolvePublicKeyType(holderId)
    }

    private fun resolvePublicKeyType(holderId: String): String {
        try {
            val publicKey = didPublicKeyResolver(holderId)
            return when (publicKey.algorithm) {
                "Ed25519" -> "Ed25519Signature2020"
                else -> "JsonWebSignature2020"
            }
        } catch (ex: Exception) {
            throw InteractiveAuthorizationException(
                "Failed to resolve public key for holderId: $holderId . ${ex.message}"
            )
        }
    }

    private suspend fun sendOVPAuthorizationResponseToIssuer(
        iar: String,
        authSession: String,
        vpResponse: Map<String, Any>
    ): InteractionResponse {
        val responseBody = mapOf(
            "auth_session" to authSession,
            "openid4vp_response" to JsonUtils.serialize(vpResponse)
        )

        val networkResponse = try {
            withContext(Dispatchers.IO) {
                NetworkManager.sendRequest(
                    url = iar,
                    method = HttpMethod.POST,
                    bodyParams = responseBody,
                    headers = mapOf(CONTENT_TYPE to APPLICATION_X_WWW_FORM_URLENCODED)
                )
            }
        } catch (ex: Exception) {
            throw InteractiveAuthorizationException("Network error while posting VP response: ${ex.message}")
        }

        return JsonUtils.deserialize(networkResponse.body, InteractionResponse::class.java)
            ?: throw InteractiveAuthorizationException("Issuer response deserialization failed")
    }
}


