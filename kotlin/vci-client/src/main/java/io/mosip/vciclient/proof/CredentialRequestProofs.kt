package io.mosip.vciclient.proof

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

data class CredentialRequestProofs(
    val proofType: String = "jwt",
    val proofs: List<String>
) {
    val firstProof: String?
        get() = proofs.firstOrNull()

    val isEmpty: Boolean
        get() = proofs.isEmpty()

    class Serializer : JsonSerializer<CredentialRequestProofs> {
        override fun serialize(
            src: CredentialRequestProofs,
            typeOfSrc: Type,
            context: JsonSerializationContext,
        ): JsonElement {
            val proofArray = JsonArray()
            src.proofs.forEach(proofArray::add)
            return JsonObject().apply {
                add(src.proofType, proofArray)
            }
        }
    }
}
