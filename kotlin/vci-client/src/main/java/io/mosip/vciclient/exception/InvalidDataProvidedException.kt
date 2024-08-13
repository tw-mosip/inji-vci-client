package io.mosip.vciclient.exception

class InvalidDataProvidedException(message: String) : Exception("Required details not provided $message")