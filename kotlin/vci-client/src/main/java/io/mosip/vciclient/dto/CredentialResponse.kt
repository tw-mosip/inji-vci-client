package io.mosip.vciclient.dto

import com.google.gson.annotations.SerializedName


sealed class CredentialResponseTypes{
    data class LdpVcResponse(val format: String, val credential: Credential)

    data class MdocVcResponse(val base64URLString: String)
}



data class Credential(
    val id: String,
    val type: ArrayList<String>,
    val proof: CredentialProof,
    val issuer: String,
    @SerializedName("@context")
    val context: ArrayList<Any>,
    val issuanceDate: String,
    val expirationDate: String?,
    val credentialSubject: Map<String, Any>
)

data class CredentialProof(
    val type: String,
    val created: String,
    val proofValue: String?,
    val proofPurpose: String,
    val verificationMethod: String,
    val jws: String?
)