package io.mosip.vciclient.dto

data class JWTProof(val proofType: String? = "jwt", val jwt: String)