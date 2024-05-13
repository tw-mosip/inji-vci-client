package io.mosip.vciclient.exception

class InvalidAccessTokenException(message: String) : Exception("Access token is invalid - $message")