package io.mosip.vciclient.dpop

internal object DPoPConstants {
    const val AUTHORIZATION_HEADER = "Authorization"
    const val DPOP_HEADER = "DPoP"
    const val DPOP_NONCE_HEADER = "DPoP-Nonce"
    const val WWW_AUTHENTICATE_HEADER = "WWW-Authenticate"

    const val DPOP_JWT_TYPE = "dpop+jwt"
    const val DPOP_TOKEN_TYPE = "DPoP"
    const val BEARER_TOKEN_TYPE = "Bearer"

    const val USE_DPOP_NONCE_ERROR = "use_dpop_nonce"

    const val HTTP_METHOD_POST = "POST"
    const val PROOF_LIFETIME_SECONDS = 60L
}
