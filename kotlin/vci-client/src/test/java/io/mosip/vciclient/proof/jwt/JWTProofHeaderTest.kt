package io.mosip.vciclient.proof.jwt

import io.mosip.vciclient.constants.JWTProofType
import io.mosip.vciclient.exception.InvalidPublicKeyException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class JWTProofHeaderTest {
    private val publicKeyPEM: String = """-----BEGIN RSA PUBLIC KEY-----
MIICCgKCAgEAi/X/H2tFMb5y5S1OG/OWLRacXo4Qos7Md0N9szlfL6vqS98DPXVY
H45q/mZKqcyJVSRSEv9fdJkhoRUi3VM60tJs+by/XnqYjs6FNtWGDoUsR52Qjj4z
NlmPBbw7eBjug3IwPmlKGntkiyJFhle+3g/voBY+mOOAZaCkG1i8/JnkLkDyr0kY
aoOfZ5gO4Q5+gT7CdN7rXzd786DZ8tsVX3VPWL+QjJ1158EcsYa3EEi3DtfCBSKI
QEjSdPAJ40rf1byPwWNi2npLIXwI0vzt8OyMn3xiYYwdq7/Oqq/6ZfStGN1uQb2i
1WVFDQks6c0vD2tdMyYMFsLBhIZxQ8F2EmYtnTawTkJknpt3vIDWOfkNNU9UtEe5
cZU0g6JZfy4R98LUnNDEtANC+dgYDj/iJ361mbGT/eIQIqUSCnLuRrk2hUbSzok6
ErI/92KoXPlyUfqRlv9YpOsIfCtetPHpKCu0hkZAU7iroTi0gDL1Ts/cpNfaAlbB
gyjrHerxHhqy8v0ZofjNatOeepnqbvOuaGjOS4sTpyvOSmADUPlCGom6jsHhLAB5
9jQ2d3jODcHPdnccAJ5IweTZ69OB/+JcpERjecmtOT0Ac9HlpxulL5tgzxFLLRqO
ptxWSQnlPIXZbrtSYFkPQOHN8Ba0o1b4iNK3AX43WFy8srpOkEPqGJcCAwEAAQ==
-----END RSA PUBLIC KEY-----"""

    @Test
    fun `should construct header with given key and algorithm and JWK`() {
        val jwtHeader: String =
            JWTProofHeader(JWTProofType.Algorithms.RS256.name, publicKeyPEM).build()

        assertEquals(
            """{
    "alg": "RS256",
    "typ": "openid4vci-proof+jwt",
    "jwk": {
        "kty": "RSA",
        "e": "AQAB",
        "use": "sig",
        "alg": "RS256",
        "n": "i_X_H2tFMb5y5S1OG_OWLRacXo4Qos7Md0N9szlfL6vqS98DPXVYH45q_mZKqcyJVSRSEv9fdJkhoRUi3VM60tJs-by_XnqYjs6FNtWGDoUsR52Qjj4zNlmPBbw7eBjug3IwPmlKGntkiyJFhle-3g_voBY-mOOAZaCkG1i8_JnkLkDyr0kYaoOfZ5gO4Q5-gT7CdN7rXzd786DZ8tsVX3VPWL-QjJ1158EcsYa3EEi3DtfCBSKIQEjSdPAJ40rf1byPwWNi2npLIXwI0vzt8OyMn3xiYYwdq7_Oqq_6ZfStGN1uQb2i1WVFDQks6c0vD2tdMyYMFsLBhIZxQ8F2EmYtnTawTkJknpt3vIDWOfkNNU9UtEe5cZU0g6JZfy4R98LUnNDEtANC-dgYDj_iJ361mbGT_eIQIqUSCnLuRrk2hUbSzok6ErI_92KoXPlyUfqRlv9YpOsIfCtetPHpKCu0hkZAU7iroTi0gDL1Ts_cpNfaAlbBgyjrHerxHhqy8v0ZofjNatOeepnqbvOuaGjOS4sTpyvOSmADUPlCGom6jsHhLAB59jQ2d3jODcHPdnccAJ5IweTZ69OB_-JcpERjecmtOT0Ac9HlpxulL5tgzxFLLRqOptxWSQnlPIXZbrtSYFkPQOHN8Ba0o1b4iNK3AX43WFy8srpOkEPqGJc"
    }
}""", jwtHeader
        )

    }

    @Test
    fun `should throw Invalid PublicKey Exception when provided public key PEM is invalid`() {
        val invalidPublicKeyPem = "invalid"

        val invalidPublicKeyException = assertThrows(
            InvalidPublicKeyException::class.java,
        ) {
            JWTProofHeader(JWTProofType.Algorithms.RS256.name, invalidPublicKeyPem)
        }

        assertEquals(
            "Invalid public key passed io.fusionauth.pem.PEMDecoderException: java.security.InvalidParameterException: Unexpected PEM Format",
            invalidPublicKeyException.message
        )
    }


}