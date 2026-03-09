package io.mosip.vciclient.networkManager

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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
}