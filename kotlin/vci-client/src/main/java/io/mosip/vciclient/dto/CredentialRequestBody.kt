package io.mosip.vciclient.dto

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName


sealed class CredentialRequestTypes {
    data class LdpVcRequestBody(
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


    data class MdocCredentialRequestBody(
        val format: String,
        val doctype: String,
        val claims: Claims,
        val proof : Proof
    ) {
        fun toJson(): String {
            val gson = GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create()
            return gson.toJson(this)!!
        }
    }

}

data class BasicClaim(val value: Any = emptyMap<String, Any>()) // Base class for claims with a single value

data class nameSpace(
    @SerializedName("given_name")
    val givenName: BasicClaim,
    @SerializedName("family_name")
    val familyName: BasicClaim,
    @SerializedName("birth_date")
    val birthData: BasicClaim
)

data class nameSpaceAAMVA(
    @SerializedName("organ_donor")
    val organDonor: BasicClaim)

// This represents the entire claims section
data class Claims(
    @SerializedName("org.iso.18013.5.1")
    val nameSpace: nameSpace,
    @SerializedName("org.iso.18013.5.1.aamva")
    val nameSpaceAAMVA: nameSpaceAAMVA
)


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