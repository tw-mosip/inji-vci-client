package io.mosip.vciclient

import io.mosip.vciclient.authorizationCodeFlow.AuthorizationMethod
import io.mosip.vciclient.authorizationCodeFlow.clientMetadata.ClientMetadata
import io.mosip.vciclient.common.Util
import io.mosip.vciclient.constants.CheckIssuerTrustCallback
import io.mosip.vciclient.constants.Constants
import io.mosip.vciclient.constants.ProofsCallback
import io.mosip.vciclient.constants.TokenResponseCallback
import io.mosip.vciclient.constants.TxCodeCallback
import io.mosip.vciclient.credential.response.CredentialResponse
import io.mosip.vciclient.credentialOffer.CredentialOfferFlowHandler
import io.mosip.vciclient.exception.VCIClientException
import io.mosip.vciclient.issuerMetadata.IssuerMetadataService
import io.mosip.vciclient.trustedIssuer.TrustedIssuerFlowHandler
import java.util.logging.Logger

class VCIClient(val traceabilityId: String) {

    private val logTag = Util.getLogTag(javaClass.simpleName, traceabilityId)
    private val logger = Logger.getLogger(logTag)

    suspend fun getIssuerMetadata(credentialIssuer: String): Map<String, Any> {
        try {
            return IssuerMetadataService().fetchAndParseIssuerMetadata(credentialIssuer)
        } catch (exception: VCIClientException) {
            logger.severe("Fetching issuer metadata failed due to ${exception.message}")
            throw VCIClientException(
                "VCI-010",
                exception.message,
                cause = exception,
                issuerErrorCode = exception.issuerErrorCode,
                issuerErrorDescription = exception.issuerErrorDescription
            )
        } catch (e: Exception) {
            logger.severe("Fetching issuer metadata failed due to ${e.message}")
            throw VCIClientException("VCI-010", "Unknown Exception - ${e.message}")
        }
    }

    suspend fun getCredentialConfigurationsSupported(credentialIssuer: String): Map<String, Any> {
        try {
            return IssuerMetadataService().fetchCredentialConfigurationsSupported(credentialIssuer)
        } catch (exception: VCIClientException) {
            logger.severe(
                "Fetching credentialConfigurationsSupported from issuer metadata failed due to ${exception.message}"
            )
            throw VCIClientException(
                "VCI-010",
                exception.message,
                cause = exception,
                issuerErrorCode = exception.issuerErrorCode,
                issuerErrorDescription = exception.issuerErrorDescription
            )
        } catch (e: Exception) {
            logger.severe("Fetching credentialConfigurationsSupported from issuer metadata failed")
            throw VCIClientException("VCI-010", "Unknown Exception - ${e.message}", cause = e)
        }
    }

    suspend fun fetchCredentialsFromTrustedIssuer(
        credentialIssuer: String,
        credentialConfigurationId: String,
        clientMetadata: ClientMetadata,
        getTokenResponse: TokenResponseCallback,
        authorizations: List<AuthorizationMethod>,
        getProofs: ProofsCallback,
        downloadTimeoutInMillis: Long = Constants.DEFAULT_NETWORK_TIMEOUT_IN_MILLIS,
    ): CredentialResponse {
        try {
            return TrustedIssuerFlowHandler().downloadCredentials(
                credentialIssuer = credentialIssuer,
                credentialConfigurationId = credentialConfigurationId,
                clientMetadata = clientMetadata,
                getTokenResponse = getTokenResponse,
                authorizationMethods = authorizations,
                getProofs = getProofs,
                downloadTimeoutInMillis = downloadTimeoutInMillis,
            )
        } catch (e: VCIClientException) {
            logger.severe("Downloading credential failed due to ${e.message}")
            throw VCIClientException(
                code = e.code,
                message = e.message,
                cause = e,
                issuerErrorCode = e.issuerErrorCode,
                issuerErrorDescription = e.issuerErrorDescription
            )
        } catch (e: Exception) {
            logger.severe("Downloading credential failed due to ${e.message}")
            throw VCIClientException("VCI-010", "Unknown Exception - ${e.message}")
        }
    }

    suspend fun fetchCredentialsUsingCredentialOffer(
        credentialOffer: String,
        clientMetadata: ClientMetadata,
        getTxCode: TxCodeCallback?,
        authorizations: List<AuthorizationMethod>,
        getTokenResponse: TokenResponseCallback,
        getProofs: ProofsCallback,
        onCheckIssuerTrust: CheckIssuerTrustCallback? = null,
        downloadTimeoutInMillis: Long = Constants.DEFAULT_NETWORK_TIMEOUT_IN_MILLIS
    ): CredentialResponse {
        try {
            return CredentialOfferFlowHandler().downloadCredentials(
                credentialOffer = credentialOffer,
                clientMetadata = clientMetadata,
                getTxCode = getTxCode,
                authorizationMethods = authorizations,
                getTokenResponse = getTokenResponse,
                getProofs = getProofs,
                onCheckIssuerTrust = onCheckIssuerTrust,
                downloadTimeoutInMillis = downloadTimeoutInMillis
            )
        } catch (e: VCIClientException) {
            logger.severe("Downloading credential failed due to ${e.message}")
            throw VCIClientException(
                code = e.code,
                message = e.message,
                cause = e,
                issuerErrorCode = e.issuerErrorCode,
                issuerErrorDescription = e.issuerErrorDescription
            )
        } catch (e: Exception) {
            logger.severe("Downloading credential failed due to ${e.message}")
            throw VCIClientException("VCI-010", "Unknown Exception - ${e.message}")
        }
    }
}
