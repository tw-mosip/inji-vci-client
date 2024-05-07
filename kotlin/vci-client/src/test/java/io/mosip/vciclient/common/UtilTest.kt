package io.mosip.vciclient.common

import org.junit.Assert.*
import org.junit.Test

class UtilTest {
    @Test
    fun `should return log tag with library name and provided class name`() {
        val traceabilityId = "test-vci-client"
        val logTag: String = Util.getLogTag(javaClass.simpleName, "$traceabilityId")

        assertEquals("INJI-VCI-Client : ${javaClass.simpleName} | traceID $traceabilityId", logTag)
    }
}