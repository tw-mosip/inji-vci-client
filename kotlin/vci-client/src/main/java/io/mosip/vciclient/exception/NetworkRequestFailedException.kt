package io.mosip.vciclient.exception

class NetworkRequestFailedException(message: String) : Exception("Download failure occurred as Network request failed, details - $message")