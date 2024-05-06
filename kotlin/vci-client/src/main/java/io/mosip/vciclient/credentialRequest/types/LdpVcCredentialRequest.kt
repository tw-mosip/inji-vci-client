package io.mosip.vciclient.credentialRequest.types

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import io.mosip.vciclient.credentialRequest.CredentialRequest
import io.mosip.vciclient.dto.IssuerMeta
import io.mosip.vciclient.dto.JWTProof
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class LdpVcCredentialRequest(
    override val accessToken: String,
    override val issuerMeta: IssuerMeta,
    override val proofJWT: String
) : CredentialRequest {
    override fun constructRequest(): Request {
        return Request.Builder()
            .url(this.issuerMeta.credentialEndpoint)
            .addHeader("Authorization", "Bearer ${this.accessToken}")
            .addHeader("Content-Type", "application/json")
            .post(generateRequestBody())
            .build()
    }

    private fun generateRequestBody(): RequestBody {
        val credentialRequestBody = CredentialRequestBody(
            credentialDefinition = CredentialDefinition(type = this.issuerMeta.credentialType),
            proof = JWTProof(jwt = this.proofJWT),
            format = this.issuerMeta.credentialFormat.value
        ).toJson()
        return credentialRequestBody
            .toRequestBody("application/json".toMediaTypeOrNull())
    }
}

internal data class CredentialRequestBody(
    val format: String,
    val credentialDefinition: CredentialDefinition,
    val proof: JWTProof,
) {
    fun toJson(): String {
        val gson = GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()
        return gson.toJson(this)!!
    }
}

internal data class CredentialDefinition(
    @SerializedName("@context")
    val context: Array<String>? = arrayOf("https://www.w3.org/2018/credentials/v1"),
    val type: Array<String>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CredentialDefinition

        if (context != null) {
            if (other.context == null) return false
            if (!context.contentEquals(other.context)) return false
        } else if (other.context != null) return false
        if (!type.contentEquals(other.type)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = context?.contentHashCode() ?: 0
        result = 31 * result + type.contentHashCode()
        return result
    }
}