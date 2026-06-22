package io.mosip.vciclient.exception

open class VCIClientException : Exception {

    val code: String
    val issuerErrorCode: String?
    val issuerErrorDescription: String?

    override val message: String
        get() = super.message ?: ""

    constructor(
        code: String,
        message: String,
        issuerErrorCode: String? = null,
        issuerErrorDescription: String? = null,
        cause: Throwable? = null
    ) : super(message, cause) {
        this.code = extractRootCode(cause) ?: code
        this.issuerErrorCode = issuerErrorCode
        this.issuerErrorDescription = issuerErrorDescription
    }

    companion object {
        private fun extractRootCode(cause: Throwable?): String? {
            var current = cause
            var lastCode: String? = null

            while (current != null) {
                if (current is VCIClientException) {
                    lastCode = current.code
                }
                current = current.cause
            }

            return lastCode
        }
    }
}
