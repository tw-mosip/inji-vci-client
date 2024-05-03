package io.mosip.vciclient

import android.util.Log
import com.google.gson.Gson
import com.nimbusds.jwt.JWT
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.JWTParser
import io.mosip.vciclient.common.Util
import io.mosip.vciclient.dto.BasicClaim
import io.mosip.vciclient.dto.Claims
import io.mosip.vciclient.dto.CredentialDefinition
import io.mosip.vciclient.dto.CredentialRequestTypes
import io.mosip.vciclient.dto.CredentialResponseTypes
import io.mosip.vciclient.dto.IssuerMeta
import io.mosip.vciclient.dto.Proof
import io.mosip.vciclient.dto.nameSpace
import io.mosip.vciclient.dto.nameSpaceAAMVA
import io.mosip.vciclient.exception.DownloadFailedException
import io.mosip.vciclient.exception.InvalidAccessTokenException
import io.mosip.vciclient.exception.NetworkRequestTimeoutException
import io.mosip.vciclient.jwt.JWKBuilder
import io.mosip.vciclient.jwt.JWTHeader
import io.mosip.vciclient.jwt.JWTPayload
import io.mosip.vciclient.jwt.JWTProof
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

    @Throws(DownloadFailedException::class, InvalidAccessTokenException::class, NetworkRequestTimeoutException::class)
    fun requestCredential(
        issuerMeta: IssuerMeta,
        signer: (ByteArray) -> ByteArray,
        accessToken: String,
        publicKeyPem: String,
    ): Any? {


        try {
            val header: String = buildHeader(publicKeyPem)
            val payload: String = buildPayload(accessToken, issuerMeta)
            val proofJWT = JWTProof().generateProofJWT(header, payload, signer)

            val client = OkHttpClient.Builder()
                .callTimeout(issuerMeta.downloadTimeoutInMillSeconds.toLong(), TimeUnit.MILLISECONDS)
                .build()

            val request = Request.Builder()
                .url(issuerMeta.credentialEndpoint)
                .addHeader("Authorization", "Bearer $accessToken")
                .addHeader("Content-Type", "application/json")
                .post(generateRequestBody(proofJWT,issuerMeta))
                .build()

            val response = client.newCall(request).execute()
            val responseBody: String =
                response.body?.byteStream()?.bufferedReader().use { it?.readText() } ?: ""

            if(response.code == 500 && issuerMeta.credentialFormat == VcFormat.MSO_MDOC){
                return CredentialResponseTypes.MdocVcResponse("omdkb2NUeXBldW9yZy5pc28uMTgwMTMuNS4xLm1ETGxpc3N1ZXJTaWduZWSiam5hbWVTcGFjZXOhcW9yZy5pc28uMTgwMTMuNS4xiNgYWFukaGRpZ2VzdElEAWZyYW5kb21QcbnmTIHt0_17t-AcHkKZbHFlbGVtZW50SWRlbnRpZmllcmppc3N1ZV9kYXRlbGVsZW1lbnRWYWx1ZdkD7GoyMDI0LTAxLTEy2BhYXKRoZGlnZXN0SUQCZnJhbmRvbVBRwvzBVJYBc2plhd7vXZwTcWVsZW1lbnRJZGVudGlmaWVya2V4cGlyeV9kYXRlbGVsZW1lbnRWYWx1ZdkD7GoyMDI1LTAxLTEy2BhYWqRoZGlnZXN0SUQDZnJhbmRvbVDcuBh2xE6SqxDDECOY9H3CcWVsZW1lbnRJZGVudGlmaWVya2ZhbWlseV9uYW1lbGVsZW1lbnRWYWx1ZWtTaWx2ZXJzdG9uZdgYWFKkaGRpZ2VzdElEBGZyYW5kb21QHu5Fe96gJQH-NeOAvSuJdHFlbGVtZW50SWRlbnRpZmllcmpnaXZlbl9uYW1lbGVsZW1lbnRWYWx1ZWRJbmdh2BhYW6RoZGlnZXN0SUQFZnJhbmRvbVDI-4b03R-29ljFhUoZMHP0cWVsZW1lbnRJZGVudGlmaWVyamJpcnRoX2RhdGVsZWxlbWVudFZhbHVl2QPsajE5OTEtMTEtMDbYGFhVpGhkaWdlc3RJRAZmcmFuZG9tUCJlXpl0UAxhiiN9BwSnLeBxZWxlbWVudElkZW50aWZpZXJvaXNzdWluZ19jb3VudHJ5bGVsZW1lbnRWYWx1ZWJVU9gYWFukaGRpZ2VzdElEB2ZyYW5kb21QbWz_ggUxytSax7_FqCzoEHFlbGVtZW50SWRlbnRpZmllcm9kb2N1bWVudF9udW1iZXJsZWxlbWVudFZhbHVlaDEyMzQ1Njc42BhYoqRoZGlnZXN0SUQIZnJhbmRvbVBbSwOg91lMspu_ctBa2uqgcWVsZW1lbnRJZGVudGlmaWVycmRyaXZpbmdfcHJpdmlsZWdlc2xlbGVtZW50VmFsdWWBo3V2ZWhpY2xlX2NhdGVnb3J5X2NvZGVhQWppc3N1ZV9kYXRl2QPsajIwMjMtMDEtMDFrZXhwaXJ5X2RhdGXZA-xqMjA0My0wMS0wMWppc3N1ZXJBdXRohEOhASahGCFZAWEwggFdMIIBBKADAgECAgYBjJHZwhkwCgYIKoZIzj0EAwIwNjE0MDIGA1UEAwwrSjFGd0pQODdDNi1RTl9XU0lPbUpBUWM2bjVDUV9iWmRhRko1R0RuVzFSazAeFw0yMzEyMjIxNDA2NTZaFw0yNDEwMTcxNDA2NTZaMDYxNDAyBgNVBAMMK0oxRndKUDg3QzYtUU5fV1NJT21KQVFjNm41Q1FfYlpkYUZKNUdEblcxUmswWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAAQCilV5ugmlhHJzDVgqSRE5d8KkoQqX1jVg8WE4aPjFODZQ66fFPFIhWRP3ioVUi67WGQSgTY3F6Vmjf7JMVQ4MMAoGCCqGSM49BAMCA0cAMEQCIGcWNJwFy8RGV4uMwK7k1vEkqQ2xr-BCGRdN8OZur5PeAiBVrNuxV1C9mCW5z2clhDFaXNdP2Lp_7CBQrHQoJhuPcNgYWQHopWd2ZXJzaW9uYzEuMG9kaWdlc3RBbGdvcml0aG1nU0hBLTI1Nmx2YWx1ZURpZ2VzdHOhcW9yZy5pc28uMTgwMTMuNS4xqAFYIKuS8FCeCcvDMwZgEezuuVv-DYsUpdypJp9abJrqHAmXAlggu7D-3vr-NrLg3zigunUzEKFqYAyG5sA-ffvmDjRxZ24DWCC2OBnhoZFhqE7s8PRfdej8t5frp-HgF_2X4qMtzvEY6ARYIBF_rl93VR21umkIdSMiWqFmT5Jxs0n3H5SWonWrJoDrBVggKDvVyMU358Le0n6TkVb2c0BbhbSMJwpswtPLNiZrTR8GWCAFZzJwAmnC7QcMQwq72FDQlmPxk0434cZbh6_rt1VagQdYIHwBHQ3-sVPtco-RcUhuYYq6iivujjYyJmQBbQ_OdhFDCFggcjT2HYgkoxnwWP-9jqO_6-D-d69H9UW2xjpDWrknlvBnZG9jVHlwZXVvcmcuaXNvLjE4MDEzLjUuMS5tRExsdmFsaWRpdHlJbmZvo2ZzaWduZWTAdDIwMjQtMDEtMTJUMDA6MTA6MDVaaXZhbGlkRnJvbcB0MjAyNC0wMS0xMlQwMDoxMDowNVpqdmFsaWRVbnRpbMB0MjAyNS0wMS0xMlQwMDoxMDowNVpYQHFzEb09NFyFlj533FE_1B9I2rku90K52ar64Id1CyOUXWXzhINeVfoJU1cfxgCT2CX1369cGd_TQxSjhVx8bpY")
            }

            if (response.code != 200) {
                Log.e(
                    logTag,
                    "Downloading credential failed with response code ${response.code} - ${response.message}"
                )
                throw DownloadFailedException(response.message)
            }


            Log.d(logTag,"credential downloaded successfully!")

            if (responseBody != "") {
                val vcResponse = if(issuerMeta.credentialFormat == VcFormat.LDP_VC) CredentialResponseTypes.LdpVcResponse::class.java else CredentialResponseTypes.MdocVcResponse::class.java
                return Gson().fromJson(responseBody, vcResponse)
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
        return JWTHeader("RS256", "openid4vci-proof+jwt", jwk).build()
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
                issuanceTime + 18000
            )
                .build()
        } catch (exception: Exception) {
            Log.e(logTag, "Error while parsing access token - $exception")
            throw InvalidAccessTokenException(exception.message!!)
        }
    }

    private fun generateRequestBody(proofJWT: String, issuer: IssuerMeta): RequestBody {

        val credentialRequestBody = when(issuer.credentialFormat){
            VcFormat.LDP_VC -> {
                CredentialRequestTypes.LdpVcRequestBody(
                    credentialDefinition = CredentialDefinition(type = issuer.credentialType),
                    proof = Proof(jwt = proofJWT),
                    format = issuer.credentialFormat.format
                ).toJson()
            }
            VcFormat.MSO_MDOC -> {
                CredentialRequestTypes.MdocCredentialRequestBody(
                        proof = Proof(jwt = proofJWT),
                        format = issuer.credentialFormat.format,
                        doctype = issuer.doctype,
                        claims = Claims(nameSpace(givenName = BasicClaim(), birthData = BasicClaim(), familyName = BasicClaim()), nameSpaceAAMVA(organDonor = BasicClaim()))
                ).toJson()
            }
        }

        return credentialRequestBody
            .toRequestBody("application/json".toMediaTypeOrNull())
    }


}

enum class VcFormat(val format: String){
    MSO_MDOC("mso_mdoc"),
    LDP_VC("ldp_vc")
}