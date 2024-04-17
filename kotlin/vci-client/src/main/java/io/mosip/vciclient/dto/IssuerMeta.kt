package io.mosip.vciclient.dto

 class IssuerMeta(
    val credentialAudience: String,
    val credentialEndpoint: String,
    val downloadTimeoutInMillSeconds: Int,
    val credentialType: Array<String>,
    val credentialFormat: String
)



