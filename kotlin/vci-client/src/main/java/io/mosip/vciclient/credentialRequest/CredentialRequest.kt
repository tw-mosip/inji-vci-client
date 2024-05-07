package io.mosip.vciclient.credentialRequest

import io.mosip.vciclient.proof.Proof
import io.mosip.vciclient.dto.IssuerMetaData
import okhttp3.Request

interface CredentialRequest {
    val accessToken: String
    val issuerMetaData: IssuerMetaData
    val proof: Proof

    fun constructRequest(): Request
}