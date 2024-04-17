package com.example.vciclient.util

import android.net.Uri

class Constants {
    companion object {
        const val DOWNLOAD_TIMEOUT: Int = 30000
        const val CREDENTIAL_ENDPOINT: String = "<credential-endpoint>"

        const val CLIENT_ID = "<client-id>"
        val URL_AUTHORIZATION: Uri = Uri.parse("<authorization-endpoint>")
        val URL_AUTH_REDIRECT: Uri = Uri.parse("<redirect-uri>")
        val URL_TOKEN_EXCHANGE: Uri =
            Uri.parse("<token-endpoint>")
        const val SCOPE = "<scope>"
        const val CREDENTIAL_AUDIENCE = "<credential-audience>"
        val CREDENTIAL_TYPE = arrayOf("<credential-type1>", "<credential-type1>")
        const val CREDENTIAL_FORMAT = "<credential-format>"
    }
}
