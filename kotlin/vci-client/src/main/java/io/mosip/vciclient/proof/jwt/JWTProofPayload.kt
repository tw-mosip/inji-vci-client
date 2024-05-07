package io.mosip.vciclient.proof.jwt

import org.json.JSONObject

class JWTProofPayload(iss: String, nonce: String, aud: String, iat: Long, exp: Long) {
    private val payload = JSONObject(
        (mutableMapOf(
            "iss" to iss,
            "nonce" to nonce,
            "aud" to aud,
            "iat" to iat,
            "exp" to exp
        ) as Map<*, *>?)!!
    )

    fun build(): String = payload.toString(4)
}