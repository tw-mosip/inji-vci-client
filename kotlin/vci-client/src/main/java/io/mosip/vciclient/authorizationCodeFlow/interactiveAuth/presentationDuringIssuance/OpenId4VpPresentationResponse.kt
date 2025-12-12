package io.mosip.vciclient.authorizationCodeFlow.interactiveAuth.presentationDuringIssuance

import com.google.gson.annotations.SerializedName
import io.mosip.vciclient.authorizationCodeFlow.interactiveAuth.response.InteractiveAuthorizationResponse
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okio.ByteString
import okio.ByteString.Companion.decodeBase64

data class OpenId4VpPresentationResponse(
    @SerializedName("status")
    override val status: String,
    @SerializedName("type")
    override val type: String,
    @SerializedName("auth_session")
    override val authSession: String,
    @SerializedName("openid4vp_request")
    val openid4vpRequest: Map<String, Any>
) : InteractiveAuthorizationResponse(status, type, authSession) {

    override fun validate() {

        if (type != "openid4vp_presentation") {
            throw IllegalArgumentException("Invalid type: expected 'openid4vp_presentation'")
        }

        if (openid4vpRequest.isEmpty()) {
            throw IllegalArgumentException("openid4vpRequest must not be empty")
        }

        if (openid4vpRequest.containsKey("request")) {
            validateSignedRequest()
        } else {
            validateUnsignedRequest()
        }
    }

    private fun validateUnsignedRequest() {
        val responseType = openid4vpRequest["response_type"] as? String
            ?: throw IllegalArgumentException("Missing or invalid 'response_type'")
        if (responseType != "vp_token") {
            throw IllegalArgumentException("response_type must be 'vp_token'")
        }

        val responseMode = openid4vpRequest["response_mode"] as? String
            ?: throw IllegalArgumentException("Missing or invalid 'response_mode'")
        if (responseMode !in listOf("iar_post", "iar_post.jwt")) {
            throw IllegalArgumentException("response_mode must be 'iar_post' or 'iar_post.jwt'")
        }


        val nonce = openid4vpRequest["nonce"] as? String
            ?: throw IllegalArgumentException("Missing or invalid 'nonce'")
        if (nonce.isBlank()) {
            throw IllegalArgumentException("nonce must not be blank")
        }
    }

    private fun validateSignedRequest() {
        val jwt = openid4vpRequest["request"] as? String
            ?: throw IllegalArgumentException("Missing or invalid 'request' JWT")

        val claims = decodeJwtPayload(jwt)

        val aud = claims["aud"] as? String
            ?: throw IllegalArgumentException("Missing 'aud' in signed JWT")
        if (!aud.startsWith("iar:")) {
            throw IllegalArgumentException("aud must start with 'iar:'")
        }

        val expectedOrigins = claims["expected_origins"] as? List<*>
            ?: throw IllegalArgumentException("Missing 'expected_origins' in signed JWT")
        if (expectedOrigins.size != 1 || expectedOrigins[0] !is String) {
            throw IllegalArgumentException("expected_origins must contain exactly one origin string")
        }
    }

    private fun decodeJwtPayload(jwt: String): Map<String, Any?> {
        val payloadJson: ByteString =
            jwt.split(".")[1].decodeBase64() ?: throw IllegalArgumentException("Invalid JWT format")
        val json = Json.parseToJsonElement(payloadJson.utf8()).jsonObject

        return json.mapValues { (_, value) ->
            when (value) {
                is JsonPrimitive -> value.contentOrNull
                is JsonArray -> value.map { it.jsonPrimitive.contentOrNull }
                is JsonObject -> value.toMap()
            }
        }
    }
}