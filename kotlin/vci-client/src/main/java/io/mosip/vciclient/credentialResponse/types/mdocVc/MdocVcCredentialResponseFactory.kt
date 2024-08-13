package io.mosip.vciclient.credentialResponse.types.mdocVc

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import io.mosip.vciclient.common.FileWriter
import io.mosip.vciclient.common.JsonUtils
import io.mosip.vciclient.common.Util
import io.mosip.vciclient.credentialResponse.CredentialResponse
import io.mosip.vciclient.credentialResponse.CredentialResponseFactory
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import java.util.function.Consumer

class MdocVcCredentialResponseFactory : CredentialResponseFactory {
    private val logTag = Util.getLogTag(javaClass.simpleName)
    override fun constructResponse(response: String): CredentialResponse {
        val mdocJsonString = MdocParser.decodeAndParseMDocData(response).toString()
        val mdocInJson = Json.decodeFromString<JsonObject>(mdocJsonString)
//        largeLog(logTag, "mdoc json ${mdocInJson.jsonObject.get("issuerSigned")?.jsonObject?.get("nameSpaces")}")
        largeLog(logTag, "mdoc json ${mdocInJson}")
        /*mdocInJson.entries.forEach(Consumer {
            val key = it.key
            println("$key is json ${it.value is JsonObject}")
            parse(it.value, 1)
        })

        val data =
            mdocInJson["issuerSigned"]?.jsonObject?.get("nameSpaces")?.jsonObject?.get("org.iso.18013.5.1")?.jsonArray
        if (data != null) {
            for (dataElement in data) {
                val jsonObject = dataElement.jsonObject
                println("${jsonObject["elementIdentifier"]}: ${jsonObject["elementValue"]}, random - ${jsonObject["random"]}")
            }
        }*/
        return JsonUtils.deserialize(
            mdocJsonString,
            MdocCredential::class.java
        )!!
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun parse(value: JsonElement, space: Int) {
        when (value) {
            is JsonObject -> {
                value.jsonObject.entries.forEach(Consumer {
                    val key = it.key
                    println("${printSpace(space)}$key")
                    parse(it.value, space + 1)
                })
            }

            is JsonArray -> {
                value.jsonArray.forEach(Consumer {
                    println("-> Array")
                    parse(it, space + 1)
                })
            }

            else -> {
                println("-> string")
            }
        }
    }

    private fun printSpace(space: Int): String {
        var result = ""
        var i = space
        while (i > 0) {
            result += " ";
            i -= 1
        }
        return result
    }


}

fun largeLog(tag: String?, content: String) {
    if (content.length > 4000) {
        Log.d(tag, content.substring(0, 4000))
        largeLog(tag, content.substring(4000))
    } else {
        Log.d(tag, content)
    }
}