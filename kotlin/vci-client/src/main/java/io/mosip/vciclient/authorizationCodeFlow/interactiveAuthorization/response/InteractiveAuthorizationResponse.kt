package io.mosip.vciclient.authorizationCodeFlow.interactiveAuthorization.response

abstract class InteractiveAuthorizationResponse(
    @Transient
    open val status: String?,
    @Transient
    open val type: String?,
    @Transient
    open val authSession: String?,
) {

    init {
        validateCommonFields()
    }

    private fun validateCommonFields() {
        if (status.isNullOrBlank()) {
            throw IllegalArgumentException("Missing or empty 'status' field")
        }

        if (status == "require_interaction") {
            if (type.isNullOrBlank()) {
                throw IllegalArgumentException("'type' is required when status is 'require_interaction'")
            }
            if (authSession.isNullOrBlank()) {
                throw IllegalArgumentException("'authSession' is required when status is 'require_interaction'")
            }
        }
    }

    abstract fun validate()
}