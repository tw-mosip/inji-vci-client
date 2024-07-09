package io.mosip.vciclient.credentialResponse.types.mdocVc


import android.util.Base64
import android.util.Log
import co.nstant.`in`.cbor.CborDecoder
import co.nstant.`in`.cbor.model.Array
import co.nstant.`in`.cbor.model.DataItem
import co.nstant.`in`.cbor.model.MajorType
import com.google.gson.annotations.SerializedName
import io.mosip.vciclient.common.JsonUtils
import io.mosip.vciclient.common.Util
import io.mosip.vciclient.credentialResponse.CredentialResponse
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import java.io.ByteArrayInputStream
import java.util.LinkedList
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.forEach
import kotlin.collections.forEachIndexed
import co.nstant.`in`.cbor.model.Array as CborArray
import co.nstant.`in`.cbor.model.ByteString as CborByteString
import co.nstant.`in`.cbor.model.Map as CborMap
import co.nstant.`in`.cbor.model.UnicodeString as CborUnicodeString

private const val ISSUER_SIGNED = "issuerSigned"

private const val NAMESPACES = "nameSpaces"

private const val ELEMENT_IDENTIFIER = "elementIdentifier"

private const val ELEMENT_VALUE = "elementValue"

private const val ISSUER_AUTH = "issuerAuth"

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
                val validityInfoJsonObject = parseIssuerAuth(cbors)
                put("validityInfo", validityInfoJsonObject)
                val credentialSubjectJsonObject = parseIssuerSigned(cbors)
                put("credentialSubject", credentialSubjectJsonObject)

            }
            return mDocVcJsonObject
        }


        private fun parseIssuerSigned(cbors: MutableList<DataItem>): JsonObject {
            val namespaces = cbors[0][ISSUER_SIGNED][NAMESPACES] as CborMap

            Log.d(logTag, "namespaces -> ${namespaces.keys}")
            var parsedIssuerSigned = buildJsonObject {  }

            namespaces.keys.forEach { namespace ->
                run {
                    val elements =
                        namespaces[namespace] as CborArray
                    parsedIssuerSigned = buildJsonObject {
                        for (item in elements.dataItems) {
                            val decoded =
                                CborDecoder(ByteArrayInputStream((item as CborByteString).bytes)).decode()

                            val identifier = decoded[0][ELEMENT_IDENTIFIER].toString()
                            val value = decoded[0][ELEMENT_VALUE]

                            when (value.majorType) {
                                MajorType.UNICODE_STRING -> put(identifier, value.toString())
                                MajorType.ARRAY -> {
                                    var parsedArray = buildJsonObject { }
                                    (value as Array).dataItems.forEach { arrayValue: DataItem ->
                                        parsedArray = buildJsonObject {
                                            (arrayValue as CborMap).keys.forEach { key ->
                                                val identifier: String = key.toString()
                                                run {
                                                    put(
                                                        identifier,
                                                        arrayValue[identifier].toString()
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    putJsonArray(identifier) {
                                        add(parsedArray)
                                    }
                                }

                                else -> {
                                    put(identifier, value.toString())
                                }
                            }
                        }
                    }
                }
            }
            return parsedIssuerSigned
        }

        private fun parseIssuerAuth(cbors: MutableList<DataItem>): JsonObject {


            val validityJsonObjectResult = buildJsonObject {

                val elements = cbors[0][ISSUER_SIGNED][ISSUER_AUTH] as CborArray



                elements.dataItems.forEachIndexed { index, dataItem ->
                    if (index == 2) {
                        val decodedIssuerAuth =
                            CborDecoder(ByteArrayInputStream((dataItem as CborByteString).bytes)).decode()
                        val validityInfo: DataItem = decodedIssuerAuth[0]["validityInfo"]
                        val validityInfoMap: co.nstant.`in`.cbor.model.Map = validityInfo as CborMap

                        Log.d(
                            logTag,
                            "validityInfo $validityInfo ${validityInfoMap.keys}"
                        )
                        val validityInfoKeys = validityInfoMap.keys as LinkedList<*>
                        validityInfoKeys.forEach { validityInfoKey: Any? ->
                            run {
                                put(
                                    validityInfoKey.toString(),
                                    validityInfo[validityInfoKey.toString()].toString()
                                )
                            }

                        }
                    }

                }
            }
            return validityJsonObjectResult
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