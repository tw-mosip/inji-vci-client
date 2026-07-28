package io.mosip.vciclient.common

import java.net.URI

/**
 * Shared construction of `.well-known` metadata URLs, kept in one place so the
 * authorization-server discovery and issuer-metadata paths cannot drift apart in this
 * security-sensitive URL handling.
 */
object WellKnownUrl {

    /**
     * RFC 8414 section 3: insert the well-known [suffix] between the authority and the path of
     * [baseUrl]. e.g. `https://host/tenant` + `/.well-known/x` -> `https://host/.well-known/x/tenant`.
     * When [baseUrl] has no path this is equivalent to appending the suffix.
     */
    fun withInsertedSuffix(baseUrl: String, suffix: String): String {
        val uri = URI(baseUrl)
        val path = uri.path?.trimEnd('/').orEmpty()
        return "${uri.scheme}://${uri.authority}$suffix$path"
    }
}
