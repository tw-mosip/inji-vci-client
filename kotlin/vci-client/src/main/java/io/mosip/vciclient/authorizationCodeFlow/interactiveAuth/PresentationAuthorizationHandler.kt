package io.mosip.vciclient.authorizationCodeFlow.interactiveAuth

import io.mosip.openID4VP.OpenID4VP
import io.mosip.openID4VP.authorizationRequest.AuthorizationRequest
import io.mosip.openID4VP.authorizationRequest.Verifier
import io.mosip.openID4VP.authorizationResponse.unsignedVPToken.UnsignedVPToken
import io.mosip.openID4VP.authorizationResponse.vpTokenSigningResult.VPTokenSigningResult
import io.mosip.openID4VP.constants.FormatType
import io.mosip.openID4VP.constants.ResponseMode
import io.mosip.vciclient.common.JsonUtils
import io.mosip.vciclient.exception.InteractiveAuthorizationException
import io.mosip.vciclient.networkManager.HttpMethod
import io.mosip.vciclient.networkManager.NetworkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class PresentationAuthorizationHandler(
    private val handlePresentationRequest: suspend (ovpRequest: AuthorizationRequest) -> Map<String, Map<FormatType, List<Any>>>,
    private val signVerifiablePresentation: suspend (
        payload: Map<FormatType, UnsignedVPToken>,
    ) -> Map<FormatType, VPTokenSigningResult>,
    private val trustedVerifiers: List<Verifier>,
    private val holderId: String? = null,
    private val signatureSuite: String? = null,
    private val shouldValidateClient: Boolean = true,
    private val openId4vp: OpenID4VP = OpenID4VP(traceabilityId = "", walletMetadata = null),
    private val handlePresentationTimeoutMs: Long = 500 * 1000L,
    private val signVPTokensTimeoutMs: Long = 5 * 1000L
) : AuthorizationHandler {

    override fun type(): String = "openid4vp_presentation"

    override suspend fun authorizeUser(requestData: AuthorizationRequestData): AuthorizationResponse {
        if (requestData !is PresentationAuthorizationRequestData) {
            return errorResponse("invalid_request", "Expected PresentationAuthorizationRequestData")
        }

        return try {
            authorize(requestData)
        } catch (ex: InteractiveAuthorizationException) {
            errorResponse(ex.code, ex.message, requestData.authSession)
        } catch (ex: Exception) {
            errorResponse("server_error", "Unexpected error occurred: ${ex.localizedMessage}")
        }
    }

    private suspend fun authorize(requestData: PresentationAuthorizationRequestData): AuthorizationResponse {
        try {
            validateAuthorizationRequest(requestData.ovpRequest)
        } catch (ex: Exception) {
            throw InteractiveAuthorizationException("Malformed authorization request. ${ex.message}")
        }

        val verifiedRequest = try {
            handlePresentation(requestData.ovpRequest)
        } catch (ex: Exception) {
            throw InteractiveAuthorizationException("Verifier is not trusted or request is invalid. ${ex.message}")
        }

        val credentialsMap = try {
            withContext(Dispatchers.IO) {
                withTimeout(handlePresentationTimeoutMs) {
                    handlePresentationRequest(verifiedRequest)
                }
            }
        } catch (ex: Exception) {
            throw InteractiveAuthorizationException("Failed to fetch matching credentials. ${ex.message}")
        }

        val unsignedVpTokens = try {
            openId4vp.constructUnsignedVPToken(
                verifiableCredentials = credentialsMap,
                holderId = this.holderId,
                signatureSuite = this.signatureSuite
            )
        } catch (ex: Exception) {
            throw InteractiveAuthorizationException("Failed to construct unsigned VP token. ${ex.message}")
        }

        val signedVpTokens = try {
            withContext(Dispatchers.IO) {
                withTimeout(signVPTokensTimeoutMs) {
                    signVerifiablePresentation(unsignedVpTokens)
                }
            }
        } catch (ex: Exception) {
            throw InteractiveAuthorizationException("Failed to sign VP token. ${ex.message}")
        }

        val vpResponse = try {
            openId4vp.constructVPResponse(vpTokenSigningResults = signedVpTokens, responseModeAlias = ResponseMode.IAR_POST)
        } catch (ex: Exception) {
            throw InteractiveAuthorizationException("Failed to construct VP response. ${ex.message}")
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

    private fun validateAuthorizationRequest(request: Map<String, Any>) {
        // Add required field validation if needed
        // throw InteractiveAuthorizationException("invalid_request", "Missing field: client_id")
    }

    private fun handlePresentation(request: Map<String, Any>): AuthorizationRequest {
        return openId4vp.authenticateVerifier(request, trustedVerifiers, shouldValidateClient)
    }

    private fun sendOVPAuthorizationResponseToIssuer(
        iar: String, authSession: String, vpResponse: Map<String, Any>
    ): AuthorizationResponse {
        val response = try {
            NetworkManager.sendRequest(
                url = iar,
                method = HttpMethod.POST,
                bodyParams = mapOf(
                    "openid4vp_response" to JsonUtils.serialize(vpResponse),
                    authSession to authSession
                )
            )
        } catch (ex: Exception) {
            throw InteractiveAuthorizationException(
                "Network error while posting VP response. ${ex.message}",
            )
        }

        val authorizationResponse = JsonUtils.deserialize(
            response.body, AuthorizationResponse::class.java
        )

        return authorizationResponse ?: throw InteractiveAuthorizationException(
            "Issuer response deserialization failed."
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
}

