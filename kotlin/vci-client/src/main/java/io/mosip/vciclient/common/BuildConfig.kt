package io.mosip.vciclient.common

import android.os.Build

object BuildConfig {
    fun getVersionSDKInt(): Int {
        return Build.VERSION.SDK_INT
    }
}