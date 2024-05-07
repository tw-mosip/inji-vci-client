package io.mosip.vciclient.common

import java.nio.charset.StandardCharsets

class Util {
    companion object {
        private lateinit var traceabilityId: String
        fun getLogTag(className: String, traceabilityId: String?= null): String {
            if (traceabilityId != null) {
                this.traceabilityId = traceabilityId
            }
            return "INJI-VCI-Client : $className | traceID ${this.traceabilityId}"
        }

        fun toByteArray(content: String): ByteArray {
            return content.toByteArray(StandardCharsets.UTF_8)
        }
    }
}