package io.mosip.vciclient.exception

class InvalidDataProvidedException : VCIClientException {

    constructor(message: String?) : super(
        code = "VCI-004",
        message = "Required details not provided $message"
    )

    constructor(
        message: String?,
        issuerErrorCode: String? = null,
        issuerErrorDescription: String? = null,
        cause: Throwable? = null
    ) : super(
        code = "VCI-004",
        message = "Required details not provided $message",
        issuerErrorCode = issuerErrorCode,
        issuerErrorDescription = issuerErrorDescription,
        cause = cause
    )
}