package io.mosip.vciclient.constants

import io.mosip.openID4VP.authorizationRequest.AuthorizationRequest
import io.mosip.openID4VP.authorizationResponse.unsignedVPToken.UnsignedVPToken
import io.mosip.openID4VP.authorizationResponse.vpTokenSigningResult.VPTokenSigningResult
import io.mosip.openID4VP.constants.FormatType
import io.mosip.vciclient.token.TokenRequest
import io.mosip.vciclient.token.TokenResponse

typealias TxCodeCallback = (suspend (inputMode: String?, description: String?, length: Int?) -> String)
typealias AuthorizeUserCallback = (suspend (authorizationUrl: String) -> String)
typealias TokenResponseCallback = suspend (tokenRequest: TokenRequest) -> TokenResponse
typealias ProofJwtCallback = (suspend (
    credentialIssuer: String,
    cNonce: String?,
    proofSigningAlgorithmsSupported: List<String>
) -> String)

typealias CheckIssuerTrustCallback = (suspend (credentialIssuer: String, issuerDisplay: List<Map<String, Any>>) -> Boolean)
typealias CredentialSelectionCallback = (suspend (ovpRequest: AuthorizationRequest) -> Map<String, Map<FormatType, List<Any>>>)
typealias SignPresentationCallback = suspend (
    payload: Map<FormatType, UnsignedVPToken>,
) -> Map<FormatType, VPTokenSigningResult>
typealias OpenWebPageCallback = (suspend (authorizationUrl: String) -> Map<String, Any>)