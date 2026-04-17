package io.mosip.vciclient.proof.jwt

import io.mosip.vciclient.constants.ProofType
import io.mosip.vciclient.proof.Proof

data class JWTProof(
    var jwt: String = "",
) : Proof {
    override val proofType: String = ProofType.JWT.value
}
