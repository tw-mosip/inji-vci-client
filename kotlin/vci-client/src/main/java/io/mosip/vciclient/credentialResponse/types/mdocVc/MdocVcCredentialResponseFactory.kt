package io.mosip.vciclient.credentialResponse.types.ldpVc

import android.util.Log
import io.mosip.vciclient.common.JsonUtils
import io.mosip.vciclient.common.Util
import io.mosip.vciclient.credentialResponse.CredentialResponse
import io.mosip.vciclient.credentialResponse.CredentialResponseFactory
import io.mosip.vciclient.credentialResponse.types.mdocVc.MdocCredential
import io.mosip.vciclient.credentialResponse.types.mdocVc.MdocParser

class MdocVcCredentialResponseFactory : CredentialResponseFactory {
    private val logTag = Util.getLogTag(javaClass.simpleName)
    override fun constructResponse(response: String): CredentialResponse {
        val mdocJsonString = MdocParser.decodeAndParseMDocData(response).toString()
        largeLog(logTag, "mdoc json $mdocJsonString")
        return JsonUtils.deserialize(
            mdocJsonString,
            MdocCredential::class.java
        )!!
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