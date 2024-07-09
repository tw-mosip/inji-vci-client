package io.mosip.vciclient.common

import android.annotation.SuppressLint
import android.os.Build
import java.nio.charset.StandardCharsets
import java.util.Base64

class Encoder {
    @SuppressLint("NewApi")
    fun encodeToBase64UrlFormat(content: String): String {
        val encodedContentByteArray: String? =
            if (BuildConfig.getVersionSDKInt() >= Build.VERSION_CODES.O) {
                Base64.getUrlEncoder().withoutPadding().encodeToString(
                    content.toByteArray(
                        StandardCharsets.UTF_8
                    )
                )
            } else {
                return android.util.Base64.encodeToString(
                    content.toByteArray(Charsets.UTF_8),
                    android.util.Base64.NO_PADDING
                )

            }
        return encodedContentByteArray!!
    }

    fun decodeBase64UrlFormat(content: String): ByteArray? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Base64.getUrlDecoder().decode(content.toByteArray())
        } else {
            println("else")
            return "".toByteArray()
        }
    }

    @SuppressLint("NewApi")
    fun encodeToBase64UrlFormat(content: ByteArray): String {
        val encodedContentByteArray: String =
            if (BuildConfig.getVersionSDKInt() >= Build.VERSION_CODES.O) {
                Base64.getUrlEncoder().withoutPadding().encodeToString(content)
            } else {
                android.util.Base64.encodeToString(content, android.util.Base64.NO_PADDING)
            }
        return encodedContentByteArray
    }
}