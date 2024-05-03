package io.mosip.vciclient.dto

import io.mosip.vciclient.VcFormat

class IssuerMeta(
    val credentialAudience: String,
    val credentialEndpoint: String,
    val downloadTimeoutInMillSeconds: Int,
    val credentialType: Array<String>,
    val credentialFormat: VcFormat,
   val doctype: String = ""
)



