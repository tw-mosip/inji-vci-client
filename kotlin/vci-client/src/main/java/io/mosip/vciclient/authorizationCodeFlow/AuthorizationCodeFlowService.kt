package io.mosip.vciclient.authorizationCodeFlow

import io.mosip.vciclient.authorizationCodeFlow.clientMetadata.ClientMetadata
import io.mosip.vciclient.authorizationCodeFlow.interactiveAuthorization.handler.InteractionType
import io.mosip.vciclient.authorizationCodeFlow.interactiveAuthorization.handler.InteractiveAuthorizationHandler
import io.mosip.vciclient.authorizationCodeFlow.interactiveAuthorization.redirectToWeb.RedirectToWebAuthorizationMethodService
import io.mosip.vciclient.authorizationServer.AuthorizationServerMetadata
import io.mosip.vciclient.authorizationServer.AuthorizationServerResolver
import io.mosip.vciclient.constants.AuthorizeUserCallback
import io.mosip.vciclient.constants.Constants
import io.mosip.vciclient.constants.ProofJwtCallback
import io.mosip.vciclient.constants.TokenResponseCallback
import io.mosip.vciclient.credential.request.CredentialRequestExecutor
import io.mosip.vciclient.credential.response.CredentialResponse
import io.mosip.vciclient.credentialOffer.CredentialOffer
import io.mosip.vciclient.exception.DownloadFailedException
import io.mosip.vciclient.issuerMetadata.IssuerMetadata
import io.mosip.vciclient.pkce.PKCESessionManager
import io.mosip.vciclient.proof.jwt.JWTProof
import io.mosip.vciclient.token.TokenResponse
import io.mosip.vciclient.token.TokenService
import java.util.logging.Logger

internal class AuthorizationCodeFlowService(
    private val authorizationServerResolver: AuthorizationServerResolver = AuthorizationServerResolver(),
    private val tokenService: TokenService = TokenService(),
    private val credentialExecutor: CredentialRequestExecutor = CredentialRequestExecutor(),
    private val pkceSessionManager: PKCESessionManager = PKCESessionManager(),
    private val interactiveAuthorizationHandler: InteractiveAuthorizationHandler = InteractiveAuthorizationHandler()
) {
    private val logger: Logger = Logger.getLogger(javaClass.simpleName)

    suspend fun requestCredentials(
        issuerMetadata: IssuerMetadata,
        credentialConfigurationId: String,
        clientMetadata: ClientMetadata,
        getTokenResponse: TokenResponseCallback,
        getProofJwt: ProofJwtCallback,
        credentialOffer: CredentialOffer? = null,
        downloadTimeOutInMillis: Long = Constants.DEFAULT_NETWORK_TIMEOUT_IN_MILLIS,
        jwtProofAlgorithmsSupported: List<String>,
        authorizeUser: AuthorizeUserCallback? = null,
        authorizationMethods: List<AuthorizationMethod>? = emptyList()
    ): CredentialResponse {
        try {
            val pkceSession = pkceSessionManager.createSession()

            val authorizationServerMetadata = try {
                authorizationServerResolver.resolveForAuthCode(issuerMetadata, credentialOffer)
            } catch (e: Exception) {
                throw DownloadFailedException(
                    "Failed to resolve authorization server metadata for issuer ${issuerMetadata.credentialIssuer}: ${e.message} "
                )
            }

            val token = try {
                performAuthorizationAndGetToken(
                    authorizationServerMetadata = authorizationServerMetadata,
                    issuerMetadata = issuerMetadata,
                    clientMetadata = clientMetadata,
                    pkceSession = pkceSession,
                    getTokenResponse = getTokenResponse,
                    credentialConfigurationId = credentialConfigurationId,
                    authorizeUser = authorizeUser,
                    authorizationMethods = authorizationMethods
                )
            } catch (e: DownloadFailedException) {
                throw e
            } catch (e: Exception) {
                throw DownloadFailedException(
                    "Failed to obtain access token via authorization code flow: ${e.message}"
                )
            }

            val jwt = try {
                getProofJwt(
                    issuerMetadata.credentialIssuer,
                    token.cNonce,
                    jwtProofAlgorithmsSupported
                )
            } catch (e: Exception) {
                throw DownloadFailedException(
                    "Failed to obtain proof JWT from callback: ${e.message}"
                )
            }

            val proof = JWTProof(jwt)

            val credentialResponse =
                credentialExecutor.requestCredential(
                    issuerMetadata = issuerMetadata,
                    credentialConfigurationId = credentialConfigurationId,
                    proof = proof,
                    accessToken = token.accessToken,
                    downloadTimeoutInMillis = downloadTimeOutInMillis
                )

            return credentialResponse
                ?: throw DownloadFailedException("Credential request returned null.")
        } catch (e: DownloadFailedException) {
            throw e
        } catch (e: Exception) {
            throw DownloadFailedException(
                "Download failed via authorization code flow: ${e.message}"
            )
        }
    }

    private suspend fun performAuthorizationAndGetToken(
        authorizationServerMetadata: AuthorizationServerMetadata,
        issuerMetadata: IssuerMetadata,
        clientMetadata: ClientMetadata,
        pkceSession: PKCESessionManager.PKCESession,
        getTokenResponse: TokenResponseCallback,
        credentialConfigurationId: String,
        authorizeUser: AuthorizeUserCallback? = null,
        authorizationMethods: List<AuthorizationMethod>? = emptyList()
    ): TokenResponse {
        val tokenEndpoint = issuerMetadata.tokenEndpoint
            ?: authorizationServerMetadata.tokenEndpoint
            ?: throw DownloadFailedException(
                "Missing token endpoint for issuer ${issuerMetadata.credentialIssuer}"
            )

        val authCode = obtainAuthorizationCode(
            authorizationServerMetadata = authorizationServerMetadata,
            issuerMetadata = issuerMetadata,
            clientMetadata = clientMetadata,
            pkceSession = pkceSession,
            credentialConfigurationId = credentialConfigurationId,
            authorizeUser = authorizeUser,
            authorizationMethods = authorizationMethods
        )

        return try {
            tokenService.getAccessToken(
                getTokenResponse = getTokenResponse,
                tokenEndpoint = tokenEndpoint,
                authCode = authCode,
                clientId = clientMetadata.clientId,
                redirectUri = clientMetadata.redirectUri,
                codeVerifier = pkceSession.codeVerifier
            )
        } catch (e: Exception) {
            throw DownloadFailedException(
                "Failed to exchange authorization code for access token at $tokenEndpoint: ${e.message}",
            )
        }
    }

    private suspend fun obtainAuthorizationCode(
        authorizationServerMetadata: AuthorizationServerMetadata,
        issuerMetadata: IssuerMetadata,
        clientMetadata: ClientMetadata,
        pkceSession: PKCESessionManager.PKCESession,
        credentialConfigurationId: String,
        authorizeUser: AuthorizeUserCallback? = null,
        authorizationMethods: List<AuthorizationMethod>?
    ): String {
        val interactiveEndpoint = authorizationServerMetadata.interactiveAuthorizationEndpoint
        val hasInteractiveAuthorizationMethods = !authorizationMethods.isNullOrEmpty()

        return when {
            interactiveEndpoint != null && hasInteractiveAuthorizationMethods -> {
                obtainAuthorizationCodeViaInteractiveEndpoint(
                    endpoint = interactiveEndpoint,
                    issuerMetadata = issuerMetadata,
                    clientMetadata = clientMetadata,
                    pkceSession = pkceSession,
                    credentialConfigurationId = credentialConfigurationId,
                    authorizationMethods = authorizationMethods!!
                )
            }

            else -> {
                obtainAuthorizationCodeViaStandardRedirectToWeb(
                    authorizationServerMetadata = authorizationServerMetadata,
                    issuerMetadata = issuerMetadata,
                    clientMetadata = clientMetadata,
                    pkceSession = pkceSession,
                    authorizeUser = authorizeUser,
                    authorizationMethods = authorizationMethods
                )
            }
        }
    }

    private suspend fun obtainAuthorizationCodeViaInteractiveEndpoint(
        endpoint: String,
        issuerMetadata: IssuerMetadata,
        clientMetadata: ClientMetadata,
        pkceSession: PKCESessionManager.PKCESession,
        credentialConfigurationId: String,
        authorizationMethods: List<AuthorizationMethod>
    ): String {
        logger.info(
            "Using Interactive Authorization Endpoint: $endpoint for issuer=${issuerMetadata.credentialIssuer}"
        )


        val response = try {
            interactiveAuthorizationHandler.handle(
                endpoint = endpoint,
                clientMetadata = clientMetadata,
                credentialConfigurationId = credentialConfigurationId,
                authorizationMethods = authorizationMethods,
                pkceSession = pkceSession
            )
        } catch (e: Exception) {
            throw DownloadFailedException(
                "Interactive authorization failed at endpoint $endpoint : ${e.message}"
            )
        }

        return response.authorizationCode
            ?: throw DownloadFailedException(
                "Authorization failed: code not received from interactive authorization endpoint $endpoint. Error : ${response.error}, Description: ${response.errorDescription}"
            )
    }

    private suspend fun obtainAuthorizationCodeViaStandardRedirectToWeb(
        authorizationServerMetadata: AuthorizationServerMetadata,
        authorizeUser: AuthorizeUserCallback? = null,
        issuerMetadata: IssuerMetadata,
        clientMetadata: ClientMetadata,
        pkceSession: PKCESessionManager.PKCESession,
        authorizationMethods: List<AuthorizationMethod>? = null
    ): String {
        val authorizationEndpoint = authorizationServerMetadata.authorizationEndpoint
            ?: throw DownloadFailedException(
                "Missing authorization endpoint for issuer ${issuerMetadata.credentialIssuer}"
            )

        val redirectToWebAuthMethod =
            authorizationMethods
                ?.firstOrNull { it.type == InteractionType.RedirectToWeb } as? AuthorizationMethod.RedirectToWeb

        if (redirectToWebAuthMethod != null) {
            logger.info(
                "Using non-interactive authorization endpoint: $authorizationEndpoint " +
                        "(redirect_to_web) for issuer=${issuerMetadata.credentialIssuer}"
            )

            val requestData = StandardAuthorizationRequestData(
                authorizeUrl = authorizationEndpoint,
                clientMetadata = clientMetadata,
                pkceSession = pkceSession,
                scope = issuerMetadata.scope
            )

            val response = try {
                RedirectToWebAuthorizationMethodService(redirectToWebAuthMethod.openWebPage)
                    .authorizeUser(requestData)

            } catch (e: Exception) {
                throw DownloadFailedException(
                    "Redirect-to-web authorization failed at endpoint $authorizationEndpoint: ${e.message}"
                )
            }
            return response.authorizationCode
                ?: throw DownloadFailedException(
                    "Authorization code not received from non-interactive authorization endpoint $authorizationEndpoint"
                )
        } else {

            val authorizationCode = authorizeUser?.invoke(authorizationEndpoint)
                ?: throw DownloadFailedException(
                    "No authorization method available to obtain authorization code from $authorizationEndpoint"
                )

            return authorizationCode
        }
    }
}