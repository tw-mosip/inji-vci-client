package io.mosip.vciclient.proof

import com.google.gson.annotations.SerializedName
import io.mosip.vciclient.constants.JWTProofType
import io.mosip.vciclient.dto.IssuerMeta

interface Proof {
    @get:SerializedName("proof_jwt")
    val proofType: String
    fun generate(
        publicKeyPem: String,
        accessToken: String,
        issuerMeta: IssuerMeta,
        signer: (ByteArray) -> ByteArray,
        algorithm: JWTProofType.Algorithms,
    ): Proof
}