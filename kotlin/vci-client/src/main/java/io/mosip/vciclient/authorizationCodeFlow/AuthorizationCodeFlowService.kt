package io.mosip.vciclient.authorizationCodeFlow

import io.mosip.vciclient.authorizationCodeFlow.clientMetadata.ClientMetadata
import io.mosip.vciclient.authorizationCodeFlow.interactiveAuth.AuthorizationHandler
import io.mosip.vciclient.authorizationCodeFlow.interactiveAuth.InteractiveAuthorizationHandler
import io.mosip.vciclient.authorizationServer.AuthorizationServerMetadata
import io.mosip.vciclient.authorizationServer.AuthorizationServerResolver
import io.mosip.vciclient.authorizationServer.AuthorizationUrlBuilder
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
    private val interactiveAuthorizationHandler: InteractiveAuthorizationHandler = InteractiveAuthorizationHandler(),

    ) {
    private val logger: Logger = Logger.getLogger(javaClass.simpleName)
    suspend fun requestCredentials(
        issuerMetadata: IssuerMetadata,
        credentialConfigurationId: String,
        clientMetadata: ClientMetadata,
        authorizeUser: AuthorizeUserCallback? = null,
        getTokenResponse: TokenResponseCallback,
        getProofJwt: ProofJwtCallback,
        credentialOffer: CredentialOffer? = null,
        downloadTimeOutInMillis: Long = Constants.DEFAULT_NETWORK_TIMEOUT_IN_MILLIS,
        jwtProofAlgorithmsSupported: List<String>,
        interactiveAuthorizationCallbacks: List<AuthorizationHandler>? = emptyList()
    ): CredentialResponse {
        try {
            val pkceSession = pkceSessionManager.createSession()

            val authorizationServerMetadata = authorizationServerResolver.resolveForAuthCode(
                issuerMetadata, credentialOffer
            )

            val token = performAuthorizationAndGetToken(
                authorizationServerMetadata = authorizationServerMetadata,
                issuerMetadata = issuerMetadata,
                clientMetadata = clientMetadata,
                authorizeUser = authorizeUser,
                pkceSession = pkceSession,
                getTokenResponse = getTokenResponse,
                credentialConfigurationId = credentialConfigurationId,
                interactiveAuthorizationCallbacks = interactiveAuthorizationCallbacks
            )

            val jwt = getProofJwt(
                issuerMetadata.credentialIssuer, token.cNonce, jwtProofAlgorithmsSupported
            )

            val proof = JWTProof(jwt)

            return credentialExecutor.requestCredential(
                issuerMetadata = issuerMetadata,
                credentialConfigurationId = credentialConfigurationId,
                proof = proof,
                accessToken = token.accessToken,
                downloadTimeoutInMillis = downloadTimeOutInMillis
            ) ?: throw DownloadFailedException("Credential request returned null.")

        } catch (e: Exception) {
            throw DownloadFailedException("Download failed via authorization code flow: ${e.message}")
        }
    }

    private suspend fun performAuthorizationAndGetToken(
        authorizationServerMetadata: AuthorizationServerMetadata,
        issuerMetadata: IssuerMetadata,
        clientMetadata: ClientMetadata,
        authorizeUser: AuthorizeUserCallback? = null,
        pkceSession: PKCESessionManager.PKCESession,
        getTokenResponse: TokenResponseCallback,
        credentialConfigurationId: String,
        interactiveAuthorizationCallbacks: List<AuthorizationHandler>? = emptyList()
    ): TokenResponse {
        val interactiveAuthorizationEndpoint =
            authorizationServerMetadata.interactiveAuthorizationEndpoint
        val tokenEndpoint =
            issuerMetadata.tokenEndpoint ?: authorizationServerMetadata.tokenEndpoint
            ?: throw DownloadFailedException("Missing token endpoint")

        val authCode =
            if (interactiveAuthorizationEndpoint != null && interactiveAuthorizationCallbacks != null) {
                logger.info("Using Interactive Authorization Endpoint: $interactiveAuthorizationEndpoint") // replace with proper logger if needed
                interactiveAuthorizationHandler.handle(
                    endpoint = interactiveAuthorizationEndpoint,
                    clientMetadata = clientMetadata,
                    credentialConfigurationId = credentialConfigurationId,
                    interactionTypesSupported = interactiveAuthorizationCallbacks.map { it.type() },
                    callback = interactiveAuthorizationCallbacks,
                    pkceSession = pkceSession
                ).authorizationCode
                    ?: throw DownloadFailedException("Authorization code not received from interactive authorization")
            } else {
                val authorizationEndpoint = authorizationServerMetadata.authorizationEndpoint
                    ?: throw DownloadFailedException("Missing authorization endpoint")

                val authUrl = AuthorizationUrlBuilder.build(
                    baseUrl = authorizationEndpoint,
                    clientId = clientMetadata.clientId,
                    redirectUri = clientMetadata.redirectUri,
                    scope = issuerMetadata.scope,
                    state = pkceSession.state,
                    codeChallenge = pkceSession.codeChallenge,
                    nonce = pkceSession.nonce
                )

                logger.info("Using non-interactive authorization endpoint: $authorizationEndpoint")
                authorizeUser?.invoke(authUrl)
                    ?: throw DownloadFailedException("Authorize user callback is null")
            }

        return tokenService.getAccessToken(
            getTokenResponse = getTokenResponse,
            tokenEndpoint = tokenEndpoint,
            authCode = authCode,
            clientId = clientMetadata.clientId,
            redirectUri = clientMetadata.redirectUri,
            codeVerifier = pkceSession.codeVerifier
        )
    }
}