package io.mosip.vciclient.common

import org.junit.Assert.*
import org.junit.Test

class UtilTest {
    @Test
    fun `should return log tag with library name and provided class name`() {
        val logTag: String = Util.getLogTag(javaClass.simpleName, "test-vci-client")

        assertEquals("INJI-VCI-Client : UtilTest | traceID test-vci-client", logTag)
    }
}