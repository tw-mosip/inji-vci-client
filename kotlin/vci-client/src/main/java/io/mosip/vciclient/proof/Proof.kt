package io.mosip.vciclient.proof

import com.google.gson.annotations.SerializedName

interface Proof {
    @get:SerializedName("proof_type")
    val proofType: String
}
