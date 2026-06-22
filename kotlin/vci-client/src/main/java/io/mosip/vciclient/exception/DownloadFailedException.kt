package io.mosip.vciclient.exception

class DownloadFailedException : VCIClientException {

    constructor(message: String?) : super(
        code = "VCI-002",
        message = "Failed to download Credential: $message"
    )

    constructor(
        message: String?,
        issuerErrorCode: String? = null,
        issuerErrorDescription: String? = null,
        cause: Throwable? = null
    ) : super(
        code = "VCI-002",
        message = "Failed to download Credential: $message",
        issuerErrorCode = issuerErrorCode,
        issuerErrorDescription = issuerErrorDescription,
        cause = cause
    )
}
