package io.mosip.vciclient.authorizationServer

import io.mosip.vciclient.common.JsonUtils
import io.mosip.vciclient.common.WellKnownUrl
import io.mosip.vciclient.constants.Constants
import io.mosip.vciclient.exception.AuthorizationServerDiscoveryException
import io.mosip.vciclient.networkManager.HttpMethod
import io.mosip.vciclient.networkManager.NetworkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.logging.Logger

private const val OAUTH_WELL_KNOWN_URI_SUFFIX = "/.well-known/oauth-authorization-server"
private const val OPENID_WELL_KNOWN_URI_SUFFIX = "/.well-known/openid-configuration"

class AuthorizationServerDiscoveryService {
    private val logger = Logger.getLogger(javaClass.simpleName)

    /**
     * Discovers the authorization server metadata by querying the well-known endpoints.
     * Some authorization servers support the "openid-configuration" suffix while others use the
     * default "oauth-authorization-server" suffix, so both are attempted.
     *
     * Per RFC 8414 section 3, when the issuer has a path component the well-known suffix is
     * inserted between the authority and the path; the legacy form (suffix appended to the issuer)
     * is kept as a fallback for servers that expose it that way.
     * reference - https://datatracker.ietf.org/doc/html/rfc8414#section-3
     */
    suspend fun discover(baseUrl: String): AuthorizationServerMetadata = withContext(Dispatchers.IO) {
        for (wellKnownUrl in buildCandidateWellKnownUrls(baseUrl)) {
            try {
                val response = NetworkManager.sendRequest(
                    url = wellKnownUrl,
                    method = HttpMethod.GET,
                    timeoutMillis = Constants.DEFAULT_NETWORK_TIMEOUT_IN_MILLIS
                )
                if (response.body.isNotBlank()) {
                    JsonUtils.deserialize(response.body, AuthorizationServerMetadata::class.java)
                        ?.let { return@withContext it }
                }
            } catch (e: Exception) {
                logger.warning(
                    "Authorization server discovery failed at $wellKnownUrl, trying next candidate: ${e.message}"
                )
            }
        }

        throw AuthorizationServerDiscoveryException(
            "Failed to discover authorization server metadata at all well-known endpoints"
        )
    }

    /**
     * Candidate well-known URLs in priority order. When the base URL has a path component the
     * suffix is inserted between the authority and the path (RFC 8414); the legacy suffix-append
     * form is always added as a fallback for servers that expose it that way.
     */
    internal fun buildCandidateWellKnownUrls(baseUrl: String): List<String> {
        val normalized = baseUrl.trimEnd('/')
        val candidates = mutableListOf<String>()

        // RFC 8414 inserted form (a no-op vs. append when the base URL has no path).
        runCatching {
            candidates.add(WellKnownUrl.withInsertedSuffix(normalized, OAUTH_WELL_KNOWN_URI_SUFFIX))
            candidates.add(WellKnownUrl.withInsertedSuffix(normalized, OPENID_WELL_KNOWN_URI_SUFFIX))
        }

        // Legacy append form kept as a fallback.
        candidates.add("$normalized$OAUTH_WELL_KNOWN_URI_SUFFIX")
        candidates.add("$normalized$OPENID_WELL_KNOWN_URI_SUFFIX")

        return candidates.distinct()
    }
}
