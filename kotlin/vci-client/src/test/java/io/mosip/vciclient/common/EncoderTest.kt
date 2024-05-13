package io.mosip.vciclient.common

import android.os.Build
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class EncoderTest {

    @Before
    fun setUp() {
        mockkObject(BuildConfig)
    }

    @Test
    fun `should encode the given string to base64 url formal with API greater than or equal to Version O`() {
        every { BuildConfig.getVersionSDKInt() } returns Build.VERSION_CODES.O
        val encoder = Encoder()

        val base64UrlEncodedString: String = encoder.encodeToBase64UrlFormat("my test string")

        assertEquals("bXkgdGVzdCBzdHJpbmc", base64UrlEncodedString)
    }

    @Test
    fun `should encode the given string to base64 url formal with API lesser than  Version O`() {
        every { BuildConfig.getVersionSDKInt() } returns Build.VERSION_CODES.N
        mockkStatic(android.util.Base64::class)
        every { android.util.Base64.encodeToString(any(),android.util.Base64.NO_PADDING) } returns "encodedString"
        val encoder = Encoder()

        val base64UrlEncodedString: String = encoder.encodeToBase64UrlFormat("my test string")

        assertEquals("encodedString", base64UrlEncodedString)
    }

    @Test
    fun `should encode the given byteArray to base64 url formal with API greater than or equal to Version O`() {
        every { BuildConfig.getVersionSDKInt() } returns Build.VERSION_CODES.O
        val encoder = Encoder()

        val base64UrlEncodedString: String = encoder.encodeToBase64UrlFormat(byteArrayOf(0x00, 0x01, 0x02, 0x03, 0x04))

        assertEquals("AAECAwQ", base64UrlEncodedString)
    }

    @Test
    fun `should encode the given byteArray to base64 url formal with API lesser than  Version O`() {
        every { BuildConfig.getVersionSDKInt() } returns Build.VERSION_CODES.N
        mockkStatic(android.util.Base64::class)
        every { android.util.Base64.encodeToString(any(),android.util.Base64.NO_PADDING) } returns "encodedString"
        val encoder = Encoder()

        val base64UrlEncodedString: String = encoder.encodeToBase64UrlFormat(byteArrayOf(0x00, 0x01, 0x02, 0x03, 0x04))

        assertEquals("encodedString", base64UrlEncodedString)
    }
}