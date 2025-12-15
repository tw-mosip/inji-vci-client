package io.mosip.vciclient.authorizationCodeFlow.interactiveAuthorization.presentationDuringIssuance

import io.mosip.openID4VP.OpenID4VP
import io.mosip.openID4VP.authorizationRequest.AuthorizationRequest
import io.mosip.openID4VP.authorizationResponse.unsignedVPToken.UnsignedVPToken
import io.mosip.openID4VP.authorizationResponse.vpTokenSigningResult.VPTokenSigningResult
import io.mosip.openID4VP.constants.FormatType
import io.mosip.openID4VP.constants.ResponseMode
import io.mosip.vciclient.authorizationCodeFlow.interactiveAuthorization.handler.AuthorizationMethodService
import io.mosip.vciclient.authorizationCodeFlow.interactiveAuthorization.request.AuthorizationRequestData
import io.mosip.vciclient.authorizationCodeFlow.interactiveAuthorization.response.AuthorizationResponse
import io.mosip.vciclient.common.JsonUtils
import io.mosip.vciclient.exception.InteractiveAuthorizationException
import io.mosip.vciclient.networkManager.HttpMethod
import io.mosip.vciclient.networkManager.NetworkManager
import io.mosip.vercred.vcverifier.keyResolver.types.did.DidPublicKeyResolver
import kotlinx.coroutines.withTimeout

class PresentationDuringIssuanceAuthorizationMethodService(
    private val handlePresentationRequest: suspend (ovpRequest: AuthorizationRequest) -> Map<String, Map<FormatType, List<Any>>>,
    private val signVerifiablePresentation: suspend (
        payload: Map<FormatType, UnsignedVPToken>,
    ) -> Map<FormatType, VPTokenSigningResult>,
    private val openId4vp: OpenID4VP = OpenID4VP(traceabilityId = "", walletMetadata = null),
    private val didPublicKeyResolver: DidPublicKeyResolver = DidPublicKeyResolver(),
    private val handlePresentationTimeoutMs: Long = 500 * 1000L,
    private val signVPTokensTimeoutMs: Long = 5 * 1000L
) : AuthorizationMethodService {

    override fun type(): String = "openid4vp_presentation"

    override suspend fun authorizeUser(requestData: AuthorizationRequestData): AuthorizationResponse {
        if (requestData !is OpenId4VpPresentationAuthorizationRequestData) {
            return errorResponse("invalid_request", "Expected PresentationAuthorizationRequestData")
        }

        return try {
            authorize(requestData)
        } catch (ex: InteractiveAuthorizationException) {
            val errorVpResponse = constructIssuerErrorResponse(
                error = "invalid_request",
                description = ex.message
            )
            sendOVPAuthorizationResponseToIssuer(
                requestData.iar,
                requestData.authSession ?: "",
                errorVpResponse
            )
            errorResponse(ex.code, ex.message, requestData.authSession)
        } catch (ex: Exception) {
            val errorVpResponse = constructIssuerErrorResponse(
                error = "invalid_request",
                description = "Unexpected error occurred: ${ex.localizedMessage}"
            )
            sendOVPAuthorizationResponseToIssuer(
                requestData.iar,
                requestData.authSession ?: "",
                errorVpResponse
            )
            errorResponse("server_error", "Unexpected error occurred: ${ex.localizedMessage}")
        }
    }

    private suspend fun authorize(requestData: OpenId4VpPresentationAuthorizationRequestData): AuthorizationResponse {
        var authorizationRequest: AuthorizationRequest
        try {
            authorizationRequest = validateAuthorizationRequest(requestData.ovpRequest)
        } catch (ex: Exception) {
            throw InteractiveAuthorizationException("Verifier is not trusted or request is invalid. ${ex.message}")
        }

        val vpResponse = try {
            handlePresentation(authorizationRequest)
        } catch (ex: Exception) {
            throw InteractiveAuthorizationException("Failed during handling presentation. ${ex.message}")
        }
        return try {
            sendOVPAuthorizationResponseToIssuer(
                iar = requestData.iar,
                authSession = requestData.authSession.orEmpty(),
                vpResponse = vpResponse
            )
        } catch (ex: Exception) {
            throw InteractiveAuthorizationException("Failed to send response to issuer. ${ex.message}")
        }
    }

    private fun validateAuthorizationRequest(request: Map<String, Any>): AuthorizationRequest {
        return openId4vp.authenticateVerifier(request, emptyList(), false)
    }

    private suspend fun handlePresentation(request: AuthorizationRequest): Map<String, Any> {
        val credentialsMap = try {
            withTimeout(handlePresentationTimeoutMs) {
                handlePresentationRequest(request)
            }
        } catch (ex: Exception) {
            throw InteractiveAuthorizationException("Failed to fetch matching credentials. ${ex.message}")
        }

        if (credentialsMap.isEmpty()) {
            throw InteractiveAuthorizationException(
                "No credentials selected by user"
            )
        }

        val firstLdpCredential =
            findFirstLdpCredential(credentialsMap)

        val holderId = extractHolderIdForLdpVc(firstLdpCredential)
        val signatureSuite = resolveSignatureSuite(holderId)

        val unsignedVpTokens = try {
            openId4vp.constructUnsignedVPToken(
                verifiableCredentials = credentialsMap,
                holderId = holderId,
                signatureSuite = signatureSuite
            )
        } catch (ex: Exception) {
            throw InteractiveAuthorizationException("Failed to construct unsigned VP token. ${ex.message}")
        }

        val signedVpTokens = try {
            withTimeout(signVPTokensTimeoutMs) {
                signVerifiablePresentation(unsignedVpTokens)
            }

        } catch (ex: Exception) {
            throw InteractiveAuthorizationException("Failed to sign VP token. ${ex.message}")
        }

        val vpResponse = try {
            openId4vp.constructVPResponse(
                vpTokenSigningResults = signedVpTokens,
                responseModeAlias = ResponseMode.IAR_POST
            )
        } catch (ex: Exception) {
            throw InteractiveAuthorizationException("Failed to construct VP response. ${ex.message}")
        }

        return vpResponse
    }

    private fun findFirstLdpCredential(
        credentialsMap: Map<String, Map<FormatType, List<Any>>>
    ): Any? {
        return credentialsMap
            .values.firstNotNullOfOrNull { it[FormatType.LDP_VC] }
            ?.firstOrNull()
    }

    private fun extractHolderIdForLdpVc(credential: Any?): String? {
        if (credential == null) {
            return null
        }
        val vc = when (credential) {
            is String -> {
                JsonUtils.deserialize(
                    credential,
                    Map::class.java
                )
            }

            is Map<*, *> -> credential
            else -> null
        } ?: throw InteractiveAuthorizationException(
            "Failed to parse LDP VC for extracting holder ID"
        )

        val credentialSubject = vc["credentialSubject"] as? Map<*, *>
            ?: throw InteractiveAuthorizationException(
                "Missing credentialSubject in LDP VC"
            )

        val holderId = credentialSubject["id"] as? String
            ?: throw InteractiveAuthorizationException(
                "Missing credentialSubject.id in LDP VC"
            )

        if (holderId.isBlank()) {
            throw InteractiveAuthorizationException(
                "credentialSubject.id must not be blank"
            )
        }

        return holderId
    }

    private fun resolveSignatureSuite(
        holderId: String?,
    ): String? {
        if (holderId == null) return null
        return resolvePublicKeyType(holderId)
    }

    private fun resolvePublicKeyType(
        holderId: String
    ): String {
        try {
            val publicKey = didPublicKeyResolver.resolve(holderId)
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

    private fun constructIssuerErrorResponse(
        error: String, description: String
    ): Map<String, Any> {
        return mapOf(
            "error" to error,
            "errorDescription" to description
        )
    }
}

private fun sendOVPAuthorizationResponseToIssuer(
    iar: String,
    authSession: String,
    vpResponse: Map<String, Any>? = null,
    errorResponse: Map<String, Any>? = null
): AuthorizationResponse {

    if (vpResponse == null && errorResponse == null) {
        throw IllegalArgumentException("Either vpResponse or errorResponse must be provided")
    }

    val responseBody = mutableMapOf(
        "auth_session" to authSession,
        "openid4vp_response" to JsonUtils.serialize(
            vpResponse ?: errorResponse!!
        )
    )

    val networkResponse = try {
        NetworkManager.sendRequest(
            url = iar,
            method = HttpMethod.POST,
            bodyParams = responseBody,
            headers = mapOf("Content-Type" to "application/x-www-form-urlencoded")
        )
    } catch (ex: Exception) {
        throw InteractiveAuthorizationException(
            "Network error while posting VP response: ${ex.message}"
        )
    }

    val authorizationResponse = JsonUtils.deserialize(
        networkResponse.body,
        AuthorizationResponse::class.java
    )

    return authorizationResponse
        ?: throw InteractiveAuthorizationException(
            "Issuer response deserialization failed"
        )
}


private fun errorResponse(
    error: String, description: String, authSession: String? = null
): AuthorizationResponse {
    return AuthorizationResponse(
        status = "error",
        authorizationCode = null,
        error = error,
        errorDescription = description,
        authSession = authSession.orEmpty()
    )
}



