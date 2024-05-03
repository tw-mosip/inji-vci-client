package io.mosip.vciclient.credentialResponse.types.ldpVc

import com.google.gson.annotations.SerializedName
import io.mosip.vciclient.credentialResponse.CredentialResponse

data class LdpVcCredentialResponse(private val format: String, private val credential: Credential) : CredentialResponse {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LdpVcCredentialResponse

        if (format != other.format) return false
        if (credential != other.credential) return false

        return true
    }

    override fun hashCode(): Int {
        var result = format.hashCode()
        result = 31 * result + credential.hashCode()
        return result
    }
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