package io.mosip.vciclient.exception

class InteractiveAuthorizationException : VCIClientException {

    constructor(message: String?) : super(
        code = "VCI-011",
        message = "Failed to authorize via interaction: $message"
    )

    constructor(
        message: String?,
        issuerErrorCode: String? = null,
        issuerErrorDescription: String? = null,
        cause: Throwable? = null
    ) : super(
        code = "VCI-011",
        message = "Failed to authorize via interaction: $message",
        issuerErrorCode = issuerErrorCode,
        issuerErrorDescription = issuerErrorDescription,
        cause = cause
    )
}

