package io.mosip.vciclient.exception

class AuthorizationServerDiscoveryException : VCIClientException {

    constructor(message: String?) : super(
        code = "VCI-001",
        message = "Failed to discover authorization server : $message"
    )

    constructor(
        message: String?,
        issuerErrorCode: String?,
        issuerErrorDescription: String?,
        cause: Throwable? = null
    ) : super(
        code = "VCI-001",
        message = "Failed to discover authorization server : $message",
        issuerErrorCode = issuerErrorCode,
        issuerErrorDescription = issuerErrorDescription,
        cause = cause
    )
}