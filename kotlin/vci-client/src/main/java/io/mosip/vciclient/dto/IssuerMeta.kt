package io.mosip.vciclient.dto

import io.mosip.vciclient.constants.CredentialFormat

class IssuerMeta(
    val credentialAudience: String,
    val credentialEndpoint: String,
    val downloadTimeoutInMillSeconds: Int,
    val credentialType: Array<String>,
    val credentialFormat: CredentialFormat
)



