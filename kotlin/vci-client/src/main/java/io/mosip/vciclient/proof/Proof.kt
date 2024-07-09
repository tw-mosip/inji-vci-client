package io.mosip.vciclient.proof

import com.google.gson.annotations.SerializedName
import io.mosip.vciclient.constants.JWTProofType
import io.mosip.vciclient.dto.IssuerMetaData

interface Proof {
    @get:SerializedName("proof_type")
    val proofType: String
    fun generate(
        publicKeyPem: String,
        accessToken: String,
        issuerMetaData: IssuerMetaData,
        signer: (ByteArray) -> ByteArray,
        algorithm: JWTProofType.Algorithms,
    ): Proof
}