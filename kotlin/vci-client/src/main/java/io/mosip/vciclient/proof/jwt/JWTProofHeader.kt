package io.mosip.vciclient.proof.jwt

import io.fusionauth.jwks.domain.JSONWebKey
import io.mosip.vciclient.constants.JWTProofType
import org.json.JSONObject


private const val ALGORITHM = "alg"

class JWTProofHeader(algorithm: String, publicKeyPem: String) {
    private val header = JSONObject(
        (mutableMapOf(
            ALGORITHM to algorithm,
            "typ" to JWTProofType.TYPE.value,
            "jwk" to pemToJWK(publicKeyPem),
        ) as Map<*, *>?)!!
    )

    private fun pemToJWK(publicKeyPem: String): JSONObject {
        val jwkJson = JSONObject(JSONWebKey.build(publicKeyPem).toJSON())
        jwkJson.put(ALGORITHM, JWTProofType.Algorithms.RS256)
        return jwkJson
    }

    fun build(): String = header.toString(4)
}