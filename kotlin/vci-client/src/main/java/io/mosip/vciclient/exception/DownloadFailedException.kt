package io.mosip.vciclient.exception

class DownloadFailedException(message: String) : Exception("Download failed due to $message")