package io.mosip.vciclient.credentialResponse.types.mdocVc


import android.util.Base64
import co.nstant.`in`.cbor.CborDecoder
import co.nstant.`in`.cbor.model.ByteString
import co.nstant.`in`.cbor.model.DataItem
import co.nstant.`in`.cbor.model.MajorType
import co.nstant.`in`.cbor.model.NegativeInteger
import co.nstant.`in`.cbor.model.Special
import com.google.gson.annotations.SerializedName
import io.mosip.vciclient.common.JsonUtils
import io.mosip.vciclient.common.Util
import io.mosip.vciclient.credentialResponse.CredentialResponse
import io.mosip.vciclient.credentialResponse.types.ldpVc.largeLog
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
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
                                    put(identifier, parseDocType(cbor[identifier] as CborMap))
                                } else if (identifier.equals(NAMESPACES)) {
                                    put(identifier, parseArray(cbor[identifier] as CborArray))
                                } else if (identifier.equals(ISSUER_SIGNED)) {
                                    println("parseIssuerSignedNamespaces(cbor[\"nameSpaces\"] ${cbor[ISSUER_SIGNED][NAMESPACES].majorType}")
                                    println("parseIssuerSignedNamespaces(cbor[\"nameSpaces\"] ${cbor[ISSUER_SIGNED][NAMESPACES]}")
                                    put(ISSUER_SIGNED, buildJsonObject {
                                        put(
                                            NAMESPACES,
                                            parseIssuerSignedNamespaces(cbor[ISSUER_SIGNED][NAMESPACES] as CborMap)
                                        )
                                        put(
                                            ISSUER_AUTH,
                                            parseIssuerSignedIssuerAuth(cbor[ISSUER_SIGNED][ISSUER_AUTH] as CborMap)
                                        )
                                    })
                                } else if (identifier.equals(MSO)) {
                                    put(identifier, parseMap(cbor[identifier] as CborMap))
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
                    println("array el major ${arrayElement.majorType}")
                    if (arrayElement.majorType == MajorType.UNICODE_STRING)
                        add(arrayElement.toString())
                    else if (arrayElement.majorType == MajorType.MAP) {
                        println("majorType = ${arrayElement.majorType}")
                        add(parseMap(arrayElement as CborMap))
                    }

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
                            if (element.majorType == MajorType.UNICODE_STRING || element.majorType == MajorType.BYTE_STRING || element.majorType == MajorType.UNSIGNED_INTEGER)
                                put(key.toString(), element.toString())
                            else if (element.majorType == MajorType.ARRAY) {
                                println("majorType = ${element.majorType}")
                                put(key.toString(), parseArray(decoded[key] as CborArray))
                            }
                        }
                    }
                } else {
                    cbor.keys.forEach { key ->
                        val element = cbor[key]
                        run {
                            when (element.majorType) {
                                MajorType.UNICODE_STRING, MajorType.BYTE_STRING, MajorType.UNSIGNED_INTEGER -> put(
                                    key.toString(),
                                    element.toString()
                                )

                                MajorType.ARRAY -> {
                                    println("majorType = ${element.majorType}")
                                    put(key.toString(), parseArray(cbor[key] as CborArray))
                                }

                                MajorType.MAP -> {
                                    put(key.toString(), parseMap(cbor[key] as CborMap))
                                }

                                else -> {
                                    put(key.toString(), parse(cbor[key]))
                                }
                            }
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
                    return JsonPrimitive(cbor.toString())
                }

                MajorType.BYTE_STRING -> {
                    return JsonPrimitive(cbor.toString())
                }

                MajorType.UNSIGNED_INTEGER -> {
                    return JsonPrimitive(cbor.toString())
                }

                MajorType.UNICODE_STRING -> {
                    return JsonPrimitive(cbor.toString())
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

        private fun parseIssuerSignedIssuerAuth(cbor: CborMap): JsonObject {
            println("issuer auth")
            val keys = cbor.keys
            val jsonObject: JsonObject = buildJsonObject {
                keys.forEach { key ->
                    run {
                        println(cbor[key].majorType)
                        when (cbor[key].majorType) {
                            MajorType.ARRAY -> {
                                put(key.toString(), parseArray(cbor[key] as CborArray))
                            }

                            MajorType.BYTE_STRING -> {
                                put(key.toString(), (cbor[key] as ByteString).toString())
                            }

                            MajorType.NEGATIVE_INTEGER -> {
                                put(key.toString(), (cbor[key] as NegativeInteger).toString())
                            }

                            MajorType.SPECIAL -> {
                                put(key.toString(), (cbor[key] as Special).toString())
                            }

                            else -> {
                                put(key.toString(), parse(cbor[key]))
                            }
                        }
                    }
                }
            }
            println("issuer signed issuer auth json - $jsonObject")
            return jsonObject
        }

        private fun parseDocType(cbor: CborMap): JsonObject {
            val docType = buildJsonObject {
                cbor.keys.forEach { key ->
                    if (key.toString() == "value")
                        put(key.toString(), cbor[key].toString())
                }
            }
            return docType
        }


        private fun parseCBOR(cbor: DataItem): Any {
            println("cbor -> ${cbor.majorType}")
            when (cbor.majorType) {
                MajorType.ARRAY -> {
                    return parseCBOR(cbor as CborArray)
                }

                MajorType.MAP -> {
                    println("Map cbor type -> ${cbor.majorType}")
                    return parseCBOR(cbor as CborMap)
                }

                MajorType.UNICODE_STRING -> {
                    return cbor.toString()
                }

                MajorType.INVALID -> {}

                MajorType.UNSIGNED_INTEGER -> {}

                MajorType.NEGATIVE_INTEGER -> {}

                MajorType.BYTE_STRING -> {}

                MajorType.TAG -> {}

                MajorType.SPECIAL -> {}
            }
            return cbor.toString()
        }

        private fun parseCBOR(cbor: CborMap): JsonObject {
            println("parsing map")
            var toBeParsed = cbor
            println("assigned")
            println("is it encodsed -> ${cbor.keys.toString().contains("type")}")
            if (cbor.keys.toString().contains("type") && cbor["type"].equals("encodedCbor")) {
                val decoded =
                    CborDecoder(ByteArrayInputStream((cbor["value"] as CborByteString).bytes)).decode()
                toBeParsed = (decoded[0] as CborMap)
            }
            val parsedMap = buildJsonObject {
                toBeParsed.keys.forEach { key ->
                    println("key -> $key")
                    val identifier: String = key.toString()
                    println("identifier -> $identifier")
                    run {
                        var cborValue = parseCBOR(toBeParsed[identifier])
                        if (cborValue !is String) {
                            cborValue = cborValue.toString()
                        }
                        largeLog(logTag, cborValue)
                        put(
                            identifier,
                            cborValue
                        )
                    }
                }
            }
            return parsedMap
        }

        private fun parseCBOR(cbor: CborArray): JsonObject {
            return buildJsonObject {
                cbor.dataItems.forEach { arrayValue: DataItem ->
                    parseCBOR(arrayValue)
                }
            }
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
    @SerializedName("validityInfo")
    val validityInfo: Map<String, Any>,
    @SerializedName("credentialSubject")
    val credentialSubject: Map<String, Any>,
) : CredentialResponse {
    override fun toJsonString(): String {
        return JsonUtils.serialize(this)
    }
}