package io.mosip.vciclient.exception

class NetworkRequestTimeoutException : VCIClientException {

    constructor(message: String?) : super(
        code = "VCI-007",
        message = "Network request timeout - $message"
    )

    constructor(
        message: String?,
        issuerErrorCode: String? = null,
        issuerErrorDescription: String? = null,
        cause: Throwable? = null
    ) : super(
        code = "VCI-007",
        message = "Network request timeout - $message",
        issuerErrorCode = issuerErrorCode,
        issuerErrorDescription = issuerErrorDescription,
        cause = cause
    )
}