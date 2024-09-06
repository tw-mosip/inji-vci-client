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

    @SuppressLint("NewApi")
    fun decodeFromBase64UrlFormatEncoded(content: String): ByteArray {
        return if (BuildConfig.getVersionSDKInt() >= Build.VERSION_CODES.O) {
            Base64.getUrlDecoder().decode(content.toByteArray())
        } else {
            var base64: String = content.replace('-', '+').replace('_', '/')
            when (base64.length % 4) {
                2 -> base64 += "=="
                3 -> base64 += "="
                else -> {}
            }

            return android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
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