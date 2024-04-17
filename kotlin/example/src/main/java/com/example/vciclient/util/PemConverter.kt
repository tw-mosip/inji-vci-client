package com.example.vciclient.util

import org.spongycastle.asn1.x509.SubjectPublicKeyInfo
import org.spongycastle.util.io.pem.PemObject
import org.spongycastle.util.io.pem.PemWriter
import java.io.StringWriter
import java.security.PublicKey

class PemConverter(private val publicKey: PublicKey) {
    fun toPem(): String {
        return dataToPem("${publicKey.algorithm} PUBLIC KEY", publicKeyToPkcs1(publicKey))
    }

    private fun dataToPem(header: String, data: ByteArray): String {
        val pemObject = PemObject(header, data)
        val stringWriter = StringWriter()
        val pemWriter = PemWriter(stringWriter)
        pemWriter.writeObject(pemObject)
        pemWriter.close()

        return stringWriter.toString()
    }

    private fun publicKeyToPkcs1(publicKey: PublicKey): ByteArray {
        val spkInfo = SubjectPublicKeyInfo.getInstance(publicKey.encoded)
        val primitive = spkInfo.parsePublicKey()
        return primitive.encoded
    }
}