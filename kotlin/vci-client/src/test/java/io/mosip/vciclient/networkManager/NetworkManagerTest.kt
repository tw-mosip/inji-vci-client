package io.mosip.vciclient.networkManager

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import io.mosip.vciclient.exception.NetworkRequestFailedException
import io.mosip.vciclient.exception.NetworkRequestTimeoutException
import java.util.concurrent.TimeUnit

class NetworkManagerTest {
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `should return response for successful GET request`() {
        val expectedBody = "{\"success\":true}"
        server.enqueue(MockResponse().setResponseCode(200).setBody(expectedBody))
        val url = server.url("/test").toString()
        val response = NetworkManager.sendRequest(url, HttpMethod.GET, headers = mapOf())
        assertEquals(expectedBody, response.body)
    }

    @Test
    fun `should throw exception for invalid URL`() {
        assertFailsWith<Exception> {
            NetworkManager.sendRequest("http://invalid-url", HttpMethod.GET, headers = mapOf())
        }
    }


    @Test
    fun `should handle empty response body`() {
        server.enqueue(MockResponse().setResponseCode(200).setBody(""))
        val url = server.url("/empty").toString()
        val response = NetworkManager.sendRequest(url, HttpMethod.GET, headers = mapOf())
        assertEquals("", response.body)
    }

    @Test
    fun `should expose parsed server error details for json error responses`() {
        server.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setBody("""{"error":"invalid_request","error_description":"missing proof"}""")
        )

        val exception = assertFailsWith<NetworkRequestFailedException> {
            NetworkManager.sendRequest(server.url("/error").toString(), HttpMethod.GET, headers = mapOf())
        }

        assertEquals("invalid_request", exception.serverErrorCode)
        assertEquals("missing proof", exception.serverErrorDescription)
    }

    @Test
    fun `should preserve raw response body when error response is not json`() {
        server.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody("upstream failed")
        )

        val exception = assertFailsWith<NetworkRequestFailedException> {
            NetworkManager.sendRequest(server.url("/error").toString(), HttpMethod.GET, headers = mapOf())
        }

        assertNull(exception.serverErrorCode)
        assertEquals("upstream failed", exception.serverErrorDescription)
    }

    @Test
    fun `should map call timeout to NetworkRequestTimeoutException`() {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("{}")
                .setBodyDelay(2, TimeUnit.SECONDS)
        )

        val exception = assertFailsWith<NetworkRequestTimeoutException> {
            NetworkManager.sendRequest(
                server.url("/timeout").toString(),
                HttpMethod.GET,
                headers = mapOf(),
                timeoutMillis = 200
            )
        }

        assertTrue(exception.message?.isNotBlank() == true)
    }
}
