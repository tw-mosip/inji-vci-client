package io.mosip.vciclient.jwt

import android.os.Build
import androidx.annotation.RequiresApi
import io.mosip.vciclient.common.Encoder
import io.mosip.vciclient.common.Util

//TODO: Rename to JWT
class JWTProof {
    @RequiresApi(Build.VERSION_CODES.O)
    fun generateProofJWT(
        header: String,
        payload: String,
        signer: (ByteArray) -> ByteArray
    ): String {
        val encoder = Encoder()
        val encodedHeader: String =
            encoder.encodeToBase64UrlFormat(header).replace('+', '-').replace('/', '_')
        //TODO: check if this replace is necessary
        val encodedPayload: String = encoder.encodeToBase64UrlFormat(payload)

        val contentBytes =
            constructContentByteArray(
                Util.toByteArray(encodedHeader),
                Util.toByteArray(encodedPayload)
            )

        val signature: String =
            encoder.encodeToBase64UrlFormat(signer(contentBytes))

        return "$encodedHeader.$encodedPayload.$signature"
    }

    private fun constructContentByteArray(
        headerByteArray: ByteArray,
        payloadByteArray: ByteArray
    ): ByteArray {
        val contentBytes = ByteArray(headerByteArray.size + 1 + payloadByteArray.size)
        System.arraycopy(headerByteArray, 0, contentBytes, 0, headerByteArray.size)
        contentBytes[headerByteArray.size] = '.'.code.toByte()
        System.arraycopy(
            payloadByteArray,
            0,
            contentBytes,
            headerByteArray.size + 1,
            payloadByteArray.size
        )
        return contentBytes
    }
}