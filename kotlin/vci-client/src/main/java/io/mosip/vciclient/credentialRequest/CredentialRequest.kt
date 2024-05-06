package io.mosip.vciclient.credentialRequest

import io.mosip.vciclient.dto.IssuerMeta
import okhttp3.Request

interface CredentialRequest {
    val accessToken: String
    val issuerMeta: IssuerMeta
    val proofJWT: String

    fun constructRequest(): Request
}