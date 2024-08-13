package io.mosip.vciclient.credentialResponse.types.mdocVc


import android.util.Base64
import co.nstant.`in`.cbor.CborDecoder
import co.nstant.`in`.cbor.model.DataItem
import co.nstant.`in`.cbor.model.MajorType
import co.nstant.`in`.cbor.model.Special
import co.nstant.`in`.cbor.model.UnicodeString
import co.nstant.`in`.cbor.model.UnsignedInteger
import com.google.gson.annotations.SerializedName
import io.mosip.vciclient.common.JsonUtils
import io.mosip.vciclient.common.Util
import io.mosip.vciclient.credentialResponse.CredentialResponse
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import java.io.ByteArrayInputStream
import kotlin.collections.Map
import kotlin.collections.forEach
import co.nstant.`in`.cbor.model.Array as CborArray
import co.nstant.`in`.cbor.model.ByteString as CborByteString
import co.nstant.`in`.cbor.model.Map as CborMap
import co.nstant.`in`.cbor.model.UnicodeString as CborUnicodeString

private const val ISSUER_SIGNED = "issuerSigned"

private const val NAMESPACES = "nameSpaces"

private const val ISSUER_AUTH = "issuerAuth"

private const val DOCTYPE = "docType"

private const val MSO = "mso"

//TODO: Move parser to the factory
//TODO: Move parser to the factory
@Serializable
class MdocParser {


    companion object {

        private val logTag: String = Util.getLogTag(MdocParser::class.java.simpleName)

        fun decodeAndParseMDocData(base64EncodedUrl: String): JsonObject {
            println("base64EncodedUrl $base64EncodedUrl")
            val decodedData: ByteArray = Base64.decode(base64EncodedUrl, Base64.URL_SAFE)
            println("decodedData $decodedData")


            val mDocVcJsonObject = buildJsonObject {
                val cbors: MutableList<DataItem> =
                    CborDecoder(ByteArrayInputStream(decodedData)).decode()
                cbors.forEach { cbor ->
                    run {

                        (cbor as CborMap).keys.forEach { key ->
                            val identifier: String = key.toString()
                            run {
                                println("cbor id -> $identifier")
                                if (identifier.equals(DOCTYPE)) {
                                    put(identifier, parse(cbor[identifier]))
                                } else if (identifier.equals(NAMESPACES)) {
                                    put(identifier, parseArray(cbor[identifier] as CborArray))
                                } else if (identifier.equals(ISSUER_SIGNED)) {
                                    put(ISSUER_SIGNED, buildJsonObject {
                                        put(
                                            NAMESPACES,
                                            parseIssuerSignedNamespaces(cbor[ISSUER_SIGNED][NAMESPACES] as CborMap)
                                        )
                                        put(
                                            ISSUER_AUTH,
                                            parse(cbor[ISSUER_SIGNED][ISSUER_AUTH])
                                        )
                                    })
                                } else if (identifier.equals(MSO)) {
                                    put(identifier, parseMap(cbor[identifier] as CborMap))
                                } else {
                                    put(identifier, parse(cbor[identifier]))
                                }
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
            println("parsing map")
            println("cbor is $cbor ")
            val jsonObject = buildJsonObject {
                if (cbor.keys.toString().contains("type") && cbor["type"].toString()
                        .equals("encodedCbor")
                ) {
                    println("& type ${cbor["type"]}")
                    val decoded =
                        (CborDecoder(ByteArrayInputStream((cbor["value"] as CborByteString).bytes)).decode()[0]) as CborMap
                    println("decoded -> $decoded")
                    decoded.keys.forEach { key ->
                        val element = decoded[key]
                        run {
                            println("majorType = ${element.majorType}")
                            put(key.toString(), parse(element))
                        }
                    }
                } else {
                    cbor.keys.forEach { key ->
                        val element = cbor[key]
                        run {
                            put(key.toString(), parse(element))
                        }
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

        private fun parseIssuerSignedNamespaces(cbor: CborMap): JsonObject {
            println("issuer signed namespaces")

            val keys = cbor.keys
            val jsonObject = buildJsonObject {
                keys.forEach { key ->
                    run {
                        println(cbor[key].majorType)
                        put(key.toString(), parseArray(cbor[key] as CborArray))
                    }
                }
            }
            println("issuer signed namespaces jsn - $jsonObject")
            return jsonObject
        }

    }


}

operator fun DataItem.get(name: String): DataItem {
    check(this.majorType == MajorType.MAP)
    this as CborMap
    return this.get(CborUnicodeString(name))
}

operator fun DataItem.get(index: Int): DataItem {
    check(this.majorType == MajorType.ARRAY)
    this as CborArray
    return this.dataItems[index]
}

data class MdocCredential(
    @SerializedName("docType")
    val docType: Map<String, Any>,
    @SerializedName("issuerSigned")
    val issuerSigned: Map<String, Any>,
) : CredentialResponse {
    override fun toJsonString(): String {
        return JsonUtils.serialize(this)
    }
}