package io.mosip.vciclient.dpop

import io.mosip.vciclient.exception.NetworkRequestFailedException
import io.mosip.vciclient.networkManager.NetworkManager
import io.mosip.vciclient.networkManager.NetworkResponse
import okhttp3.Request

private const val HTTP_UNAUTHORIZED = 401

/**
 * Sends the credential-endpoint request with DPoP applied entirely inside the library.
 *
 * When the token response carried `token_type=DPoP`, the access token is presented with the
 * `DPoP` authorization scheme alongside a signed proof. A `use_dpop_nonce` challenge is retried
 * once with the server supplied nonce. A Bearer-only challenge triggers a best-effort Bearer
 * retry per RFC 9449 section 7.2. A Bearer token response skips DPoP altogether.
 */
class DPoPCredentialRequestSender(
    private val sendRequest: (Request, Long) -> NetworkResponse =
        { request, timeout -> NetworkManager.sendRequest(request, timeout) },
) {
    fun send(
        baseRequest: Request,
        accessToken: String,
        credentialEndpoint: String,
        tokenType: String?,
        dpopManager: DPoPManager,
        timeoutMillis: Long,
    ): NetworkResponse {
        val useDpop = dpopManager.isInitialized &&
            tokenType.equals(DPoPConstants.DPOP_TOKEN_TYPE, ignoreCase = true)

        if (!useDpop) {
            return sendRequest(baseRequest, timeoutMillis)
        }

        val dpopRequest = withDpop(
            baseRequest,
            accessToken,
            dpopManager.generateCredentialProof(credentialEndpoint, accessToken)
        )

        return try {
            sendRequest(dpopRequest, timeoutMillis)
        } catch (failure: NetworkRequestFailedException) {
            if (failure.httpStatusCode != HTTP_UNAUTHORIZED) throw failure

            val challenge = WwwAuthenticateChallenge.parse(
                failure.headers?.get(DPoPConstants.WWW_AUTHENTICATE_HEADER)
            )
            val nonce = failure.headers?.get(DPoPConstants.DPOP_NONCE_HEADER)

            when {
                challenge.error == DPoPConstants.USE_DPOP_NONCE_ERROR && nonce != null -> {
                    sendRequest(
                        withDpop(
                            baseRequest,
                            accessToken,
                            dpopManager.generateCredentialProof(credentialEndpoint, accessToken, nonce)
                        ),
                        timeoutMillis
                    )
                }

                !challenge.isDpop -> {
                    sendRequest(withBearer(baseRequest, accessToken), timeoutMillis)
                }

                else -> throw failure
            }
        }
    }

    private fun withDpop(baseRequest: Request, accessToken: String, proof: String): Request =
        baseRequest.newBuilder()
            .header(DPoPConstants.AUTHORIZATION_HEADER, "${DPoPConstants.DPOP_TOKEN_TYPE} $accessToken")
            .header(DPoPConstants.DPOP_HEADER, proof)
            .build()

    private fun withBearer(baseRequest: Request, accessToken: String): Request =
        baseRequest.newBuilder()
            .header(DPoPConstants.AUTHORIZATION_HEADER, "${DPoPConstants.BEARER_TOKEN_TYPE} $accessToken")
            .removeHeader(DPoPConstants.DPOP_HEADER)
            .build()
}
