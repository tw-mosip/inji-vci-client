package io.mosip.vciclient.exception

class InvalidPublicKeyException : VCIClientException {

    constructor(message: String?) : super(
        code = "VCI-005",
        message = "Invalid public key passed $message"
    )

    constructor(
        message: String?,
        issuerErrorCode: String? = null,
        issuerErrorDescription: String? = null,
        cause: Throwable? = null
    ) : super(
        code = "VCI-005",
        message = "Invalid public key passed $message",
        issuerErrorCode = issuerErrorCode,
        issuerErrorDescription = issuerErrorDescription,
        cause = cause
    )
}
