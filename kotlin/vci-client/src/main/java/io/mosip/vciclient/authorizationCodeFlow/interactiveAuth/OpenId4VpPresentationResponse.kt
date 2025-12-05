package io.mosip.vciclient.authorizationCodeFlow.interactiveAuth

import android.util.Base64
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.collections.get

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
        // Top-level checks
        if (status != "require_interaction") {
            throw IllegalArgumentException("Invalid status: expected 'require_interaction'")
        }

        if (type != "openid4vp_presentation") {
            throw IllegalArgumentException("Invalid type: expected 'openid4vp_presentation'")
        }

        if (authSession.isBlank()) {
            throw IllegalArgumentException("authSession must not be blank")
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
        if (responseMode !in listOf("iar-post", "iar-post.jwt")) {
            throw IllegalArgumentException("response_mode must be 'iar-post' or 'iar-post.jwt'")
        }

        val presentationDefinition = openid4vpRequest["presentation_definition"] as? Map<*, *>
            ?: throw IllegalArgumentException("Missing or invalid 'presentation_definition'")

        val inputDescriptors = presentationDefinition["input_descriptors"] as? List<*>
            ?: throw IllegalArgumentException("Missing 'input_descriptors' in presentation_definition")
        if (inputDescriptors.isEmpty()) {
            throw IllegalArgumentException("presentation_definition.input_descriptors must not be empty")
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
        val parts = jwt.split(".")
        if (parts.size != 3) throw IllegalArgumentException("Malformed JWT")

        val payloadJson = String(
            Base64.decode(
                parts[1], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
            )
        )

        val jsonElement = Json.Default.parseToJsonElement(payloadJson).jsonObject
        return jsonElement.mapValues { it.value.toString() }
    }
}