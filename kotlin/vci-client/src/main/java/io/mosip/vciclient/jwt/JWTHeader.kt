package io.mosip.vciclient.jwt

import io.fusionauth.jwks.domain.JSONWebKey
import org.json.JSONObject

class JWTHeader(algorithm: String, type: String, jwk: JSONObject) {
    private val header = JSONObject(
        (mutableMapOf(
            "alg" to algorithm,
            "typ" to type,
            "jwk" to jwk,
        ) as Map<*, *>?)!!
    )

    fun build(): String = header.toString(4)
}

class JWKBuilder {
    fun build(publicKeyPem: String): JSONObject {
        val jwkJson = JSONObject(JSONWebKey.build(publicKeyPem).toJSON())
        jwkJson.put("alg", "RS256")
        return jwkJson
    }
}