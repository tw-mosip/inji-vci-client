package io.mosip.vciclient

import android.util.Log
import io.mosip.vciclient.common.Util
import io.mosip.vciclient.constants.JWTProofType
import io.mosip.vciclient.credentialRequest.CredentialRequestFactory
import io.mosip.vciclient.credentialResponse.CredentialResponse
import io.mosip.vciclient.credentialResponse.CredentialResponseFactory
import io.mosip.vciclient.dto.IssuerMeta
import io.mosip.vciclient.exception.DownloadFailedException
import io.mosip.vciclient.exception.InvalidAccessTokenException
import io.mosip.vciclient.exception.NetworkRequestTimeoutException
import io.mosip.vciclient.proof.Proof
import io.mosip.vciclient.proof.jwt.JWTProof
import okhttp3.OkHttpClient
import java.io.InterruptedIOException
import java.util.concurrent.TimeUnit


class VCIClient {
    private val logTag = Util.getLogTag(javaClass.simpleName)

    @Throws(
        DownloadFailedException::class,
        InvalidAccessTokenException::class,
        NetworkRequestTimeoutException::class
    )
    fun requestCredential(
        issuerMeta: IssuerMeta,
        signer: (ByteArray) -> ByteArray,
        accessToken: String,
        publicKeyPem: String,
    ): CredentialResponse? {


        try {
            val proof: Proof = JWTProof().generate(
                publicKeyPem,
                accessToken,
                issuerMeta,
                signer,
                JWTProofType.Algorithms.RS256
            )

            val client = OkHttpClient.Builder()
                .callTimeout(
                    issuerMeta.downloadTimeoutInMillSeconds.toLong(),
                    TimeUnit.MILLISECONDS
                )
                .build()

            val request = CredentialRequestFactory.createCredentialRequest(
                issuerMeta.credentialFormat,
                accessToken,
                issuerMeta,
                proof
            )

            val response = client.newCall(request).execute()

            if (response.code != 200) {
                Log.e(
                    logTag,
                    "Downloading credential failed with response code ${response.code} - ${response.message}"
                )
                throw DownloadFailedException(response.message)
            }

            val responseBody: String =
                response.body?.byteStream()?.bufferedReader().use { it?.readText() } ?: ""
            Log.d(logTag, "credential downloaded successfully!")

            if (responseBody != "") {
                return CredentialResponseFactory.createCredentialResponse(
                    issuerMeta.credentialFormat,
                    responseBody
                )
            }

            Log.w(
                logTag,
                "The response body from credentialEndpoint is empty, responseCode - ${response.code}, responseMessage ${response.message}, returning null."
            )
            return null
        } catch (exception: InterruptedIOException) {
            Log.e(
                logTag,
                "Network request for ${issuerMeta.credentialEndpoint} took more than expected time(${issuerMeta.downloadTimeoutInMillSeconds / 1000}s). Exception - $exception"
            )
            throw NetworkRequestTimeoutException()
        } catch (exception: Exception) {
            if (exception is DownloadFailedException || exception is InvalidAccessTokenException)
                throw exception
            Log.e(
                logTag,
                "Downloading credential failed due to ${exception.message}"
            )
            throw DownloadFailedException(exception.message!!)
        }
    }

}