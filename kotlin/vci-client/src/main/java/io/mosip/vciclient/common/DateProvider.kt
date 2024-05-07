package io.mosip.vciclient.common

import java.util.Date

object DateProvider {
    fun getCurrentTime(): Long {
        return Date().time
    }
}