package io.mosip.vciclient.exception

class NetworkRequestFailedException : VCIClientException {

    constructor(message: String?) : super(
        code = "VCI-006",
        message = "Network request failed, details - $message"
    )

    constructor(
        message: String?,
        issuerErrorCode: String? = null,
        issuerErrorDescription: String? = null,
        cause: Throwable? = null
    ) : super(
        code = "VCI-006",
        message = "Network request failed, details - $message",
        issuerErrorCode = issuerErrorCode,
        issuerErrorDescription = issuerErrorDescription,
        cause = cause
    )
}
