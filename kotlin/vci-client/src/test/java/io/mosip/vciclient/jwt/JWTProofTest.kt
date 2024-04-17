package io.mosip.vciclient.jwt

import android.os.Build
import io.mockk.every
import io.mockk.mockkObject
import io.mosip.vciclient.common.BuildConfig
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class JWTProofTest {
    @Before
    fun setUp() {
        mockkObject(BuildConfig)
        every { BuildConfig.getVersionSDKInt() } returns Build.VERSION_CODES.O
    }

    @Test
    fun `should generate JWT for the given payload and header with the signer function passed`() {
        val header = """{
    "typ": "openid4vci-proof+jwt",
    "alg": "RS256",
    "jwk": {
        "kty": "RSA",
        "e": "AQAB",
        "use": "sig",
        "alg": "RS256",
        "n": "i_X_H2tFMb5y5S1OG_OWLRacXo4Qos7Md0N9szlfL6vqS98DPXVYH45q_mZKqcyJVSRSEv9fdJkhoRUi3VM60tJs-by_XnqYjs6FNtWGDoUsR52Qjj4zNlmPBbw7eBjug3IwPmlKGntkiyJFhle-3g_voBY-mOOAZaCkG1i8_JnkLkDyr0kYaoOfZ5gO4Q5-gT7CdN7rXzd786DZ8tsVX3VPWL-QjJ1158EcsYa3EEi3DtfCBSKIQEjSdPAJ40rf1byPwWNi2npLIXwI0vzt8OyMn3xiYYwdq7_Oqq_6ZfStGN1uQb2i1WVFDQks6c0vD2tdMyYMFsLBhIZxQ8F2EmYtnTawTkJknpt3vIDWOfkNNU9UtEe5cZU0g6JZfy4R98LUnNDEtANC-dgYDj_iJ361mbGT_eIQIqUSCnLuRrk2hUbSzok6ErI_92KoXPlyUfqRlv9YpOsIfCtetPHpKCu0hkZAU7iroTi0gDL1Ts_cpNfaAlbBgyjrHerxHhqy8v0ZofjNatOeepnqbvOuaGjOS4sTpyvOSmADUPlCGom6jsHhLAB59jQ2d3jODcHPdnccAJ5IweTZ69OB_-JcpERjecmtOT0Ac9HlpxulL5tgzxFLLRqOptxWSQnlPIXZbrtSYFkPQOHN8Ba0o1b4iNK3AX43WFy8srpOkEPqGJc"
    }
}"""
        val payload = """{
    "aud": "https://domai.env.net",
    "iss": "45453XDF",
    "exp": 1713250631,
    "nonce": "O0Ar8K73Ws3Ors9NzlO",
    "iat": 1713232631
}"""

        fun signer(input: ByteArray): ByteArray {
            return byteArrayOf(0x00, 0x01, 0x02, 0x03, 0x04)
        }

        val generatedProofJWT: String = JWTProof().generateProofJWT(header, payload, ::signer)

        assertEquals(
            "ewogICAgInR5cCI6ICJvcGVuaWQ0dmNpLXByb29mK2p3dCIsCiAgICAiYWxnIjogIlJTMjU2IiwKICAgICJqd2siOiB7CiAgICAgICAgImt0eSI6ICJSU0EiLAogICAgICAgICJlIjogIkFRQUIiLAogICAgICAgICJ1c2UiOiAic2lnIiwKICAgICAgICAiYWxnIjogIlJTMjU2IiwKICAgICAgICAibiI6ICJpX1hfSDJ0Rk1iNXk1UzFPR19PV0xSYWNYbzRRb3M3TWQwTjlzemxmTDZ2cVM5OERQWFZZSDQ1cV9tWktxY3lKVlNSU0V2OWZkSmtob1JVaTNWTTYwdEpzLWJ5X1hucVlqczZGTnRXR0RvVXNSNTJRamo0ek5sbVBCYnc3ZUJqdWczSXdQbWxLR250a2l5SkZobGUtM2dfdm9CWS1tT09BWmFDa0cxaThfSm5rTGtEeXIwa1lhb09mWjVnTzRRNS1nVDdDZE43clh6ZDc4NkRaOHRzVlgzVlBXTC1RakoxMTU4RWNzWWEzRUVpM0R0ZkNCU0tJUUVqU2RQQUo0MHJmMWJ5UHdXTmkybnBMSVh3STB2enQ4T3lNbjN4aVlZd2RxN19PcXFfNlpmU3RHTjF1UWIyaTFXVkZEUWtzNmMwdkQydGRNeVlNRnNMQmhJWnhROEYyRW1ZdG5UYXdUa0prbnB0M3ZJRFdPZmtOTlU5VXRFZTVjWlUwZzZKWmZ5NFI5OExVbk5ERXRBTkMtZGdZRGpfaUozNjFtYkdUX2VJUUlxVVNDbkx1UnJrMmhVYlN6b2s2RXJJXzkyS29YUGx5VWZxUmx2OVlwT3NJZkN0ZXRQSHBLQ3UwaGtaQVU3aXJvVGkwZ0RMMVRzX2NwTmZhQWxiQmd5anJIZXJ4SGhxeTh2MFpvZmpOYXRPZWVwbnFidk91YUdqT1M0c1RweXZPU21BRFVQbENHb202anNIaExBQjU5alEyZDNqT0RjSFBkbmNjQUo1SXdlVFo2OU9CXy1KY3BFUmplY210T1QwQWM5SGxweHVsTDV0Z3p4RkxMUnFPcHR4V1NRbmxQSVhaYnJ0U1lGa1BRT0hOOEJhMG8xYjRpTkszQVg0M1dGeThzcnBPa0VQcUdKYyIKICAgIH0KfQ.ewogICAgImF1ZCI6ICJodHRwczovL2RvbWFpLmVudi5uZXQiLAogICAgImlzcyI6ICI0NTQ1M1hERiIsCiAgICAiZXhwIjogMTcxMzI1MDYzMSwKICAgICJub25jZSI6ICJPMEFyOEs3M1dzM09yczlOemxPIiwKICAgICJpYXQiOiAxNzEzMjMyNjMxCn0.AAECAwQ",
            generatedProofJWT
        )
    }
}