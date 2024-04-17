package io.mosip.vciclient.common

import org.junit.Assert.*
import org.junit.Test

class UtilTest {
    @Test
    fun `should return log tag with library name and provided class name`() {
        val logTag: String = Util.getLogTag(javaClass.simpleName)

        assertEquals("INJI-VCI-Client : ${javaClass.simpleName}", logTag)
    }
}