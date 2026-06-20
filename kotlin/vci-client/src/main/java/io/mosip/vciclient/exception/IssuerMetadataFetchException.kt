package io.mosip.vciclient.exception

class IssuerMetadataFetchException : VCIClientException {

    constructor(message: String?) : super(
        code = "VCI-009",
        message = "Failed to fetch issuerMetadata - $message"
    )

    constructor(
        message: String?,
        issuerErrorCode: String? = null,
        issuerErrorDescription: String? = null,
        cause: Throwable? = null
    ) : super(
        code = "VCI-009",
        message = "Failed to fetch issuerMetadata: $message",
        issuerErrorCode = issuerErrorCode,
        issuerErrorDescription = issuerErrorDescription,
        cause = cause
    )
}
