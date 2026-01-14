package io.mosip.vciclient.exception

class InteractiveAuthorizationException(
    message: String?,
) : VCIClientException("VCI-011", "Failed to authorize via interaction: $message")
