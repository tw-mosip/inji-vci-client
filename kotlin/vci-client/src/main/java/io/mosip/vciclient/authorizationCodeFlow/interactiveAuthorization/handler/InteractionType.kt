package io.mosip.vciclient.authorizationCodeFlow.interactiveAuthorization.handler

enum class InteractionType(val value: String) {
    RedirectToWeb("redirect_to_web"),
    OpenId4VpPresentation("openid4vp_presentation")
}