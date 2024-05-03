package io.mosip.vciclient.jwt

import io.fusionauth.jwks.domain.JSONWebKey
import io.mosip.vciclient.constants.JWTProofType
import org.json.JSONObject


private const val ALGORITHM = "alg"

class JWTProofHeader(algorithm: String, jwk: JSONObject) {
    private val header = JSONObject(
        (mutableMapOf(
            ALGORITHM to algorithm,
            "typ" to JWTProofType.TYPE.value,
            "jwk" to jwk,
        ) as Map<*, *>?)!!
    )

    fun build(): String = header.toString(4)
}

class JWKBuilder {
    fun build(publicKeyPem: String): JSONObject {
        val jwkJson = JSONObject(JSONWebKey.build(publicKeyPem).toJSON())
        jwkJson.put(ALGORITHM, JWTProofType.Algorithms.RS256)
        return jwkJson
    }
}