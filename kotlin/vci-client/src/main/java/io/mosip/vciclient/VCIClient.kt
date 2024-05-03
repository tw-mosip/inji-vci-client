package io.mosip.vciclient

import android.util.Log
import com.google.gson.Gson
import com.nimbusds.jwt.JWT
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.JWTParser
import io.mosip.vciclient.common.Util
import io.mosip.vciclient.constants.JWTProofType
import io.mosip.vciclient.dto.CredentialDefinition
import io.mosip.vciclient.dto.CredentialRequestBody
import io.mosip.vciclient.dto.CredentialResponse
import io.mosip.vciclient.dto.IssuerMeta
import io.mosip.vciclient.dto.Proof
import io.mosip.vciclient.exception.DownloadFailedException
import io.mosip.vciclient.exception.InvalidAccessTokenException
import io.mosip.vciclient.exception.NetworkRequestTimeoutException
import io.mosip.vciclient.jwt.JWKBuilder
import io.mosip.vciclient.jwt.JWTPayload
import io.mosip.vciclient.jwt.JWTProof
import io.mosip.vciclient.jwt.JWTProofHeader
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.InterruptedIOException
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.math.floor


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
            val header: String = buildHeader(publicKeyPem)
            val payload: String = buildPayload(accessToken, issuerMeta)
            val proofJWT = JWTProof().generateProofJWT(header, payload, signer)

            val client = OkHttpClient.Builder()
                .callTimeout(
                    issuerMeta.downloadTimeoutInMillSeconds.toLong(),
                    TimeUnit.MILLISECONDS
                )
                .build()

            val request = Request.Builder()
                .url(issuerMeta.credentialEndpoint)
                .addHeader("Authorization", "Bearer $accessToken")
                .addHeader("Content-Type", "application/json")
                .post(generateRequestBody(proofJWT, issuerMeta))
                .build()

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

            /** Interface  : type(format) -> boolean
             *  -> JSONLD : construct
             */
            //TODO: Introduce interface & build response dynamically
            if (responseBody != "") {
                return Gson().fromJson(responseBody, CredentialResponse::class.java)
            }

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

    private fun buildHeader(publicKeyPem: String): String {
        val jwk: JSONObject = JWKBuilder().build(publicKeyPem)
        return JWTProofHeader(JWTProofType.Algorithms.RS256.name, jwk).build()
    }

    private fun buildPayload(accessToken: String, issuerMeta: IssuerMeta): String {
        try {
            val decodedAccessToken: JWT = JWTParser.parse(accessToken)
            val jwtClaimsSet: JWTClaimsSet = decodedAccessToken.jwtClaimsSet
            val issuanceTime: Long = floor((Date().time / 1000).toDouble()).toLong()

            return JWTPayload(
                jwtClaimsSet.getClaim("client_id").toString(),
                jwtClaimsSet.getClaim("c_nonce").toString(),
                issuerMeta.credentialAudience,
                issuanceTime,
                //TODO: Move the 18000 to constant
                issuanceTime + 18000
            )
                .build()
        } catch (exception: Exception) {
            Log.e(logTag, "Error while parsing access token - $exception")
            throw InvalidAccessTokenException(exception.message!!)
        }
    }

    private fun generateRequestBody(proofJWT: String, issuer: IssuerMeta): RequestBody {
        val credentialRequestBody = CredentialRequestBody(
            credentialDefinition = CredentialDefinition(type = issuer.credentialType),
            proof = Proof(jwt = proofJWT),
            format = issuer.credentialFormat
        ).toJson()
        return credentialRequestBody
            .toRequestBody("application/json".toMediaTypeOrNull())
    }


}