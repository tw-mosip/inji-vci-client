package io.mosip.vciclient.dpop

import com.nimbusds.jwt.SignedJWT
import io.mosip.vciclient.exception.NetworkRequestFailedException
import io.mosip.vciclient.networkManager.NetworkResponse
import okhttp3.Headers
import okhttp3.Headers.Companion.headersOf
import okhttp3.Request
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.jupiter.api.assertThrows

class DPoPCredentialRequestSenderTest {

    private val credentialEndpoint = "https://issuer.example.com/credential"
    private val accessToken = "access-token"

    private fun baseRequest(): Request = Request.Builder()
        .url(credentialEndpoint)
        .header("Authorization", "Bearer $accessToken")
        .build()

    private fun dpopManager(): DPoPManager =
        DPoPManager().apply { initialize("https://as.example.com/token", listOf("ES256")) }

    private fun ok(body: String = """{"credential":"vc"}""") = NetworkResponse(body, null)

    private fun unauthorized(headers: Headers) = NetworkRequestFailedException(
        message = "HTTP 401",
        httpStatusCode = 401,
        headers = headers
    )

    private class RecordingSender(private val outcomes: List<() -> NetworkResponse>) {
        val sent = mutableListOf<Request>()
        private var index = 0
        fun send(request: Request, @Suppress("UNUSED_PARAMETER") timeout: Long): NetworkResponse {
            sent.add(request)
            return outcomes[index++]()
        }
    }

    @Test
    fun `bearer token type is sent unchanged without dpop`() {
        val recorder = RecordingSender(listOf({ ok() }))
        val response = DPoPCredentialRequestSender(recorder::send).send(
            baseRequest = baseRequest(),
            accessToken = accessToken,
            credentialEndpoint = credentialEndpoint,
            tokenType = "Bearer",
            dpopManager = dpopManager(),
            timeoutMillis = 1000
        )

        assertNotNull(response)
        assertEquals(1, recorder.sent.size)
        assertEquals("Bearer $accessToken", recorder.sent[0].header("Authorization"))
        assertNull(recorder.sent[0].header("DPoP"))
    }

    @Test
    fun `dpop token type sends dpop authorization and proof without nonce`() {
        val recorder = RecordingSender(listOf({ ok() }))
        DPoPCredentialRequestSender(recorder::send).send(
            baseRequest = baseRequest(),
            accessToken = accessToken,
            credentialEndpoint = credentialEndpoint,
            tokenType = "DPoP",
            dpopManager = dpopManager(),
            timeoutMillis = 1000
        )

        val request = recorder.sent.single()
        assertEquals("DPoP $accessToken", request.header("Authorization"))
        val proof = request.header("DPoP")
        assertNotNull(proof)
        assertNull(SignedJWT.parse(proof).jwtClaimsSet.getStringClaim("nonce"))
    }

    @Test
    fun `use_dpop_nonce challenge is retried once with the server nonce`() {
        val recorder = RecordingSender(
            listOf(
                {
                    throw unauthorized(
                        headersOf(
                            "WWW-Authenticate", """DPoP error="use_dpop_nonce"""",
                            "DPoP-Nonce", "server-nonce"
                        )
                    )
                },
                { ok() }
            )
        )

        val response = DPoPCredentialRequestSender(recorder::send).send(
            baseRequest = baseRequest(),
            accessToken = accessToken,
            credentialEndpoint = credentialEndpoint,
            tokenType = "DPoP",
            dpopManager = dpopManager(),
            timeoutMillis = 1000
        )

        assertNotNull(response)
        assertEquals(2, recorder.sent.size)
        val retryProof = recorder.sent[1].header("DPoP")
        assertEquals("server-nonce", SignedJWT.parse(retryProof).jwtClaimsSet.getStringClaim("nonce"))
    }

    @Test
    fun `bearer only challenge triggers best effort bearer retry`() {
        val recorder = RecordingSender(
            listOf(
                { throw unauthorized(headersOf("WWW-Authenticate", """Bearer error="invalid_token"""")) },
                { ok() }
            )
        )

        DPoPCredentialRequestSender(recorder::send).send(
            baseRequest = baseRequest(),
            accessToken = accessToken,
            credentialEndpoint = credentialEndpoint,
            tokenType = "DPoP",
            dpopManager = dpopManager(),
            timeoutMillis = 1000
        )

        assertEquals(2, recorder.sent.size)
        val retry = recorder.sent[1]
        assertEquals("Bearer $accessToken", retry.header("Authorization"))
        assertNull(retry.header("DPoP"))
    }

    @Test
    fun `dpop challenge without nonce error is not downgraded`() {
        val recorder = RecordingSender(
            listOf({ throw unauthorized(headersOf("WWW-Authenticate", """DPoP error="invalid_dpop_proof"""")) })
        )

        assertThrows<NetworkRequestFailedException> {
            DPoPCredentialRequestSender(recorder::send).send(
                baseRequest = baseRequest(),
                accessToken = accessToken,
                credentialEndpoint = credentialEndpoint,
                tokenType = "DPoP",
                dpopManager = dpopManager(),
                timeoutMillis = 1000
            )
        }
        assertEquals(1, recorder.sent.size)
    }

    @Test
    fun `non 401 failures are propagated`() {
        val recorder = RecordingSender(
            listOf({ throw NetworkRequestFailedException(message = "HTTP 500", httpStatusCode = 500, headers = null) })
        )

        assertThrows<NetworkRequestFailedException> {
            DPoPCredentialRequestSender(recorder::send).send(
                baseRequest = baseRequest(),
                accessToken = accessToken,
                credentialEndpoint = credentialEndpoint,
                tokenType = "DPoP",
                dpopManager = dpopManager(),
                timeoutMillis = 1000
            )
        }
        assertEquals(1, recorder.sent.size)
    }
}
