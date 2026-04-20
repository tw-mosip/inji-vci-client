package io.mosip.vciclient.credential.request

import io.mosip.vciclient.common.JsonUtils
import io.mosip.vciclient.common.Util
import io.mosip.vciclient.credential.response.CredentialResponse
import io.mosip.vciclient.credential.response.CredentialResponseDraft13
import io.mosip.vciclient.exception.DownloadFailedException
import io.mosip.vciclient.exception.InvalidPublicKeyException
import io.mosip.vciclient.exception.NetworkRequestFailedException
import io.mosip.vciclient.exception.NetworkRequestTimeoutException
import io.mosip.vciclient.issuerMetadata.IssuerMetadata
import io.mosip.vciclient.networkManager.NetworkManager
import io.mosip.vciclient.proof.Proof
import io.mosip.vciclient.proof.CredentialRequestProofs
import java.util.logging.Logger

class CredentialRequestExecutor(
    private val factoryDraft13: CredentialRequestFactoryDraft13 = CredentialRequestFactoryDraft13(),
    private val factory: CredentialRequestFactory = CredentialRequestFactory(),
) {

    private val logTag = Util.getLogTag(javaClass.simpleName, "")
    private val logger = Logger.getLogger(logTag)

    @Throws(
        DownloadFailedException::class,
    )
    fun requestCredential(
        issuerMetadata: IssuerMetadata,
        credentialConfigurationId: String,
        proofs: CredentialRequestProofs,
        accessToken: String,
        downloadTimeoutInMillis: Long? = 10000,
    ): CredentialResponse? {
        val timeout = downloadTimeoutInMillis ?: 10000

        try {
            val request = factory.createCredentialRequest(
                accessToken,
                issuerMetadata,
                credentialConfigurationId,
                proofs
            )

            val networkResponse = NetworkManager.sendRequest(
                request = request,
                timeoutMillis = timeout
            )

            val responseBody = networkResponse.body

            logger.info("Credential downloaded successfully")

            if (responseBody.isNotBlank()) {
                val credentialResponse =
                    JsonUtils.deserialize(responseBody, CredentialResponse::class.java)
                        ?: throw DownloadFailedException("Failed to parse credential response.")

                credentialResponse.credentials?.forEachIndexed { index, item ->
                    if (item == null) {
                        throw DownloadFailedException("Invalid credential response: credentials[$index] is null.")
                    }
                    if (item.credential == null || item.credential.isJsonNull) {
                        throw DownloadFailedException("Invalid credential response: credentials[$index] is missing the 'credential' key or has a null value.")
                    }
                }

                credentialResponse.credentialConfigurationId = credentialConfigurationId
                credentialResponse.credentialIssuer = issuerMetadata.credentialIssuer

                return credentialResponse
            }

            logger.warning("Credential endpoint returned empty body")
            return null

        } catch (e: NetworkRequestTimeoutException) {
            logger.severe("Credential download timed out after ${timeout / 1000}s")
            throw DownloadFailedException(
                message = "Credential download timed out after ${timeout / 1000}s",
                cause = e,
            )
        } catch (e: NetworkRequestFailedException) {
            logger.severe("Credential download failed: ${e.message}")
            throw DownloadFailedException(
                message = e.message,
                cause = e,
                serverErrorCode = e.serverErrorCode,
                serverErrorDescription = e.serverErrorDescription
            )
        } catch (e: InvalidPublicKeyException) {
            throw DownloadFailedException(
                e.message,
                cause = e
            )
        } catch (e: DownloadFailedException) {
            throw e
        } catch (e: Exception) {
            logger.severe("Unexpected error during credential download: ${e.message}")
            throw DownloadFailedException(
                message = e.message,
                cause = e
            )
        }
    }

    fun requestCredentialDraft13(
        issuerMetadata: IssuerMetadata,
        credentialConfigurationId: String,
        proof: Proof,
        accessToken: String,
        downloadTimeoutInMillis: Long? = 10000,
    ): CredentialResponseDraft13? {

        val timeout = downloadTimeoutInMillis ?: 10000

        try {

            val request = factoryDraft13.createCredentialRequest(
                issuerMetadata.credentialFormat,
                accessToken,
                issuerMetadata,
                proof
            )

            val networkResponse = NetworkManager.sendRequest(
                request = request,
                timeoutMillis = timeout
            )

            val responseBody = networkResponse.body

            logger.info("Credential downloaded successfully")

            if (responseBody.isNotBlank()) {

                val credentialResponse =
                    JsonUtils.deserialize(responseBody, CredentialResponseDraft13::class.java)

                credentialResponse?.credentialConfigurationId = credentialConfigurationId
                credentialResponse?.credentialIssuer = issuerMetadata.credentialIssuer

                return credentialResponse
            }

            logger.warning("Credential endpoint returned empty body")
            return null

        } catch (e: NetworkRequestTimeoutException) {
            logger.severe("Credential download timed out after ${timeout / 1000}s")
            throw DownloadFailedException(
                message = "Credential download timed out after ${timeout / 1000}s",
                cause = e,
            )
        } catch (e: NetworkRequestFailedException) {
            logger.severe("Credential download failed: ${e.message}")
            throw DownloadFailedException(
                message = e.message,
                cause = e,
                serverErrorCode = e.serverErrorCode,
                serverErrorDescription = e.serverErrorDescription
            )
        } catch (e: InvalidPublicKeyException) {
            throw DownloadFailedException(
                e.message,
                cause = e
            )
        } catch (e: Exception) {
            logger.severe("Unexpected error during credential download: ${e.message}")
            throw DownloadFailedException(
                message = e.message,
                cause = e
            )
        }
    }

}
