package io.mosip.vciclient.credentialResponse.types.ldpVc

import com.google.gson.annotations.SerializedName
import io.mosip.vciclient.common.JsonUtils
import io.mosip.vciclient.credentialResponse.CredentialResponse

data class LdpVcCredentialResponse(private val format: String, private val credential: Credential) : CredentialResponse {
    override fun toJsonString(): String {
        return JsonUtils.serialize(this)
    }
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
    @SerializedName("issuanceDate")
    val issuanceDate: String,
    @SerializedName("expirationDate")
    val expirationDate: String?,
    @SerializedName("credentialSubject")
    val credentialSubject: Map<String, Any>
)

data class CredentialProof(
    val type: String,
    val created: String,
    @SerializedName("proofValue")
    val proofValue: String?,
    @SerializedName("proofPurpose")
    val proofPurpose: String,
    @SerializedName("verificationMethod")
    val verificationMethod: String,
    val jws: String?
)