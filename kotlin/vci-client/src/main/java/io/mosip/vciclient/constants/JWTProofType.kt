package io.mosip.vciclient.constants

enum class JWTProofType(val value: String) {
    TYPE("openid4vci-proof+jwt");

    enum class Algorithms {
        RS256
    }
}

