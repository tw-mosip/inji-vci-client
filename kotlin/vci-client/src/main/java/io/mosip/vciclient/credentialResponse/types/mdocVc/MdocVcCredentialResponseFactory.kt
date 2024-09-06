package io.mosip.vciclient.credentialResponse.types.mdocVc

import co.nstant.`in`.cbor.CborDecoder
import co.nstant.`in`.cbor.model.DataItem
import co.nstant.`in`.cbor.model.MajorType
import co.nstant.`in`.cbor.model.Special
import co.nstant.`in`.cbor.model.UnicodeString
import co.nstant.`in`.cbor.model.UnsignedInteger
import io.mosip.vciclient.common.Encoder
import io.mosip.vciclient.common.JsonUtils
import io.mosip.vciclient.credentialResponse.CredentialResponse
import io.mosip.vciclient.credentialResponse.CredentialResponseFactory
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import java.io.ByteArrayInputStream
import kotlin.collections.forEach
import co.nstant.`in`.cbor.model.Array as CborArray
import co.nstant.`in`.cbor.model.ByteString as CborByteString
import co.nstant.`in`.cbor.model.Map as CborMap
import co.nstant.`in`.cbor.model.UnicodeString as CborUnicodeString

class MdocVcCredentialResponseFactory : CredentialResponseFactory {
    override fun constructResponse(response: String): CredentialResponse {
        val deserializedResponse = JsonUtils.deserialize(response, HashMap::class.java)
        val mdocJson: JsonObject = decodeAndParseMDocData(
            deserializedResponse?.get("credential")
                .toString()
        )
        val mdocCredential = MdocCredential(credential = mdocJson)
        return mdocCredential
    }

    private fun decodeAndParseMDocData(base64EncodedUrl: String): JsonObject {
        val decodedData: ByteArray =
            Encoder().decodeFromBase64UrlFormatEncoded(base64EncodedUrl)

        val mDocVcJsonObject = buildJsonObject {
            val cbors: MutableList<DataItem> =
                CborDecoder(ByteArrayInputStream(decodedData)).decode()
            cbors.forEach { cbor ->
                run {
                    (cbor as CborMap).keys.forEach { key ->
                        val identifier: String = key.toString()
                        run {
                            put(identifier, parse(cbor[identifier]))
                        }
                    }
                }
            }
        }
        return mDocVcJsonObject
    }

    private fun parseArray(cbor: CborArray): JsonArray {
        val jsonArray = buildJsonArray {
            cbor.dataItems.forEach { arrayElement ->
                add(parse(arrayElement))
            }
        }
        return jsonArray
    }

    private fun parseMap(cbor: CborMap): JsonObject {
        val jsonObject = buildJsonObject {
            cbor.keys.forEach { key ->
                val element = cbor[key]
                run {
                    put(key.toString(), parse(element))
                }
            }
        }
        return jsonObject
    }

    private fun parse(cbor: DataItem): JsonElement {
        when (cbor.majorType) {
            MajorType.ARRAY -> {
                return parseArray(cbor as CborArray)
            }

            MajorType.MAP -> {
                return parseMap(cbor as CborMap)
            }

            MajorType.SPECIAL -> {
                return JsonPrimitive((cbor as Special).toString())
            }

            MajorType.BYTE_STRING -> {
                try {
                    val decoded =
                        CborDecoder(ByteArrayInputStream((cbor as CborByteString).bytes)).decode()

                    return parse(decoded[0])
                } catch (e: Exception) {
                    return JsonPrimitive(String((cbor as CborByteString).bytes))
                } catch (e: OutOfMemoryError) {
                    return JsonPrimitive(String((cbor as CborByteString).bytes))

                }
            }

            MajorType.UNSIGNED_INTEGER -> {
                return JsonPrimitive((cbor as UnsignedInteger).toString())
            }

            MajorType.UNICODE_STRING -> {
                return JsonPrimitive((cbor as UnicodeString).toString())
            }

            else -> return JsonPrimitive(null)
        }
    }


    operator fun DataItem.get(name: String): DataItem {
        check(this.majorType == MajorType.MAP)
        this as CborMap
        return this.get(CborUnicodeString(name))
    }
}