package io.mosip.vciclient.exception

class InvalidAccessTokenException : VCIClientException {

    constructor(message: String?) : super(
        code = "VCI-003",
        message = "Access token is invalid - $message"
    )

    constructor(
        message: String?,
        issuerErrorCode: String?,
        issuerErrorDescription: String?,
        cause: Throwable? = null
    ) : super(
        code = "VCI-003",
        message = "Access token is invalid - $message",
        issuerErrorCode = issuerErrorCode,
        issuerErrorDescription = issuerErrorDescription,
        cause = cause
    )
}
