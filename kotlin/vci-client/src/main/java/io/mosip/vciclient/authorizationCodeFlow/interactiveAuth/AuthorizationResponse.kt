package io.mosip.vciclient.authorizationCodeFlow.interactiveAuth

import com.google.gson.annotations.SerializedName

data class AuthorizationResponse(

    @SerializedName("authorization_code")
    val authorizationCode: String? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("error")
    val error: String? = null,

    @SerializedName("error_description")
    val errorDescription: String? = null,

    @SerializedName("auth_session")
    val authSession: String? = null
)