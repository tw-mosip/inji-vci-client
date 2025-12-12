package io.mosip.vciclient.authorizationCodeFlow

import io.mosip.vciclient.authorizationCodeFlow.interactiveAuth.handler.InteractionType
import io.mosip.vciclient.constants.CredentialSelectionCallback
import io.mosip.vciclient.constants.OpenWebPageCallback
import io.mosip.vciclient.constants.SignPresentationCallback

//TODO: check if type property is required here
sealed class AuthorizationMethod(val type: InteractionType) {

    class RedirectToWeb(
        val openWebPage: OpenWebPageCallback
    ) : AuthorizationMethod(InteractionType.RedirectToWeb)

    class PresentationDuringIssuance(
        val selectCredentialsForPresentation: CredentialSelectionCallback,
        val signVerifiablePresentation: SignPresentationCallback
    ) : AuthorizationMethod(InteractionType.OpenId4VpPresentation)
}
