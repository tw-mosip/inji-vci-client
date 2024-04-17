package io.mosip.vciclient.dto

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName

data class CredentialRequestBody(
    val format: String,
    val credentialDefinition: CredentialDefinition,
    val proof: Proof,
) {
    fun toJson(): String {
        val gson = GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()
        return gson.toJson(this)!!
    }
}

data class CredentialDefinition(
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

data class Proof(val proofType: String? = "jwt", val jwt: String)