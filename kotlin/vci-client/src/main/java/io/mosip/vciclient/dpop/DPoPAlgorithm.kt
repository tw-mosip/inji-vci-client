package io.mosip.vciclient.dpop

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.Curve

internal enum class DPoPAlgorithm(
    val algorithmName: String,
    val curve: Curve,
    val jwsAlgorithm: JWSAlgorithm,
) {
    ES256("ES256", Curve.P_256, JWSAlgorithm.ES256),
    ES384("ES384", Curve.P_384, JWSAlgorithm.ES384),
    ES512("ES512", Curve.P_521, JWSAlgorithm.ES512);

    companion object {
        private val preferenceOrder = listOf(ES256, ES384, ES512)

        fun select(authorizationServerSupported: List<String>?): DPoPAlgorithm {
            if (authorizationServerSupported.isNullOrEmpty()) return ES256
            return preferenceOrder.firstOrNull { it.algorithmName in authorizationServerSupported }
                ?: ES256
        }
    }
}
