package io.mosip.vciclient.credentialRequest.types

import com.google.gson.annotations.SerializedName
import io.mosip.vciclient.common.JsonUtils
import io.mosip.vciclient.credentialRequest.CredentialRequest
import io.mosip.vciclient.dto.IssuerMetaData
import io.mosip.vciclient.proof.Proof
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class MdocCredentialRequest(
    override val accessToken: String,
    override val issuerMetaData: IssuerMetaData,
    override val proof: Proof,
) : CredentialRequest {
    override fun constructRequest(): Request {
        return Request.Builder()
            .url(this.issuerMetaData.credentialEndpoint)
            .addHeader("Authorization", "Bearer ${this.accessToken}")
            .addHeader("Content-Type", "application/json")
            .post(generateRequestBody())
            .build()
    }

    private fun generateRequestBody(): RequestBody {
        val credentialRequestBody = MdocCredentialRequestBody(
            claims = mapOf("issuerMetaData.doctype" to mapOf("issuerMetaData.claims" to {})),
            proof = proof,
            format = this.issuerMetaData.credentialFormat.value,
            doctype = "issuerMetaData.doctype"
        ).toJson()
        return credentialRequestBody
            .toRequestBody("application/json".toMediaTypeOrNull())
    }
}

private data class MdocCredentialRequestBody(
    val format: String,
    val doctype: String,
    val claims: Map<String, Any>,
    val proof: Proof,
) {
    fun toJson(): String {
        return JsonUtils.serialize(this)
    }
}