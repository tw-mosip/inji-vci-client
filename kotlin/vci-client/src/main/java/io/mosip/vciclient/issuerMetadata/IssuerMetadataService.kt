package io.mosip.vciclient.issuerMetadata

import io.mosip.vciclient.common.JsonUtils
import io.mosip.vciclient.constants.Constants
import io.mosip.vciclient.constants.CredentialFormat
import io.mosip.vciclient.constants.NetworkContentType.APPLICATION_JSON
import io.mosip.vciclient.constants.NetworkHeader.ACCEPT
import io.mosip.vciclient.exception.IssuerMetadataFetchException
import io.mosip.vciclient.networkManager.HttpMethod
import io.mosip.vciclient.networkManager.NetworkManager

private const val WELL_KNOWN_OPENID_CREDENTIAL_ISSUER = "/.well-known/openid-credential-issuer"

class IssuerMetadataService {
    private var cachedIssuerMetadataResult: IssuerMetadataResult? = null
    private var timeoutMillis: Long
    private var session: NetworkManager

    constructor(){
        this.session = NetworkManager
        this.timeoutMillis = Constants.DEFAULT_NETWORK_TIMEOUT_IN_MILLIS // Default timeout in milliseconds
    }

    constructor(session: NetworkManager = NetworkManager, timeoutMillis: Long){
        this.session = session
        this.timeoutMillis = timeoutMillis
    }

    fun fetchAndParseIssuerMetadata(credentialIssuerUri: String): Map<String, Any> {
        val wellKnownUrl = "$credentialIssuerUri$WELL_KNOWN_OPENID_CREDENTIAL_ISSUER"
        val response = NetworkManager.sendRequest(
            url = wellKnownUrl,
            method = HttpMethod.GET,
            headers = mapOf(ACCEPT.value to APPLICATION_JSON.value),
            timeoutMillis = this.timeoutMillis
        )

        val body = response.body
        if (body.isBlank()) {
            throw IssuerMetadataFetchException("Issuer metadata response is empty.")
        }

        return try {
            JsonUtils.toMap(body)
        } catch (e: Exception) {
            throw IssuerMetadataFetchException("Issuer metadata is not valid JSON.")
        }
    }

    /**
     * Fetches and resolves issuer metadata for a given issuer URI and credential configuration ID.
     * Stores the result in a cache (member property) to avoid redundant network calls.
     */
    fun fetchIssuerMetadataResult(
        issuerUri: String,
        credentialConfigurationId: String
    ): IssuerMetadataResult {
        cachedIssuerMetadataResult?.let { cached ->
            if (cached.issuerUri == issuerUri) {
                return cached
            }
        }

        val rawIssuerMetadata = fetchAndParseIssuerMetadata(issuerUri)
        val resolved = resolveMetadata(credentialConfigurationId, rawIssuerMetadata)

        val result = IssuerMetadataResult(
            issuerMetadata = resolved,
            raw = rawIssuerMetadata,
            issuerUri = issuerUri
        )
        cachedIssuerMetadataResult = result
        return result
    }

    private fun resolveMetadata(
        credentialConfigurationId: String,
        rawIssuerMetadata: Map<String, Any>,
    ): IssuerMetadata {
        val credentialConfigurationsSupported = rawIssuerMetadata["credential_configurations_supported"] as? Map<*, *>
            ?: throw IssuerMetadataFetchException("Missing credential_configurations_supported")

        val credentialType = credentialConfigurationsSupported[credentialConfigurationId] as? Map<*, *>
            ?: throw IssuerMetadataFetchException("Credential configuration not found: $credentialConfigurationId")

        val format = credentialType["format"] as? String
        val credentialEndpoint = rawIssuerMetadata["credential_endpoint"] as? String
            ?: throw IssuerMetadataFetchException("Missing credential_endpoint")

        return when (format) {
            CredentialFormat.MSO_MDOC.value -> {
                val doctype = credentialType["doctype"] as? String
                    ?: throw IssuerMetadataFetchException("Missing doctype")
                val claims = credentialType["claims"] as? Map<String, Any>

                IssuerMetadata(
                    credentialAudience = rawIssuerMetadata["credential_issuer"] as String,
                    credentialEndpoint = credentialEndpoint,
                    credentialFormat = CredentialFormat.MSO_MDOC,
                    doctype = doctype,
                    claims = claims,
                    authorizationServers = rawIssuerMetadata["authorization_servers"] as List<String>
                )
            }

            CredentialFormat.LDP_VC.value -> {
                val credentialDefinition = credentialType["credential_definition"]!! as? Map<*, *>
                val types = credentialDefinition!!["type"] as? List<String>
                val context = credentialDefinition["@context"] as? List<String>
                val scope = credentialType["scope"] as? String
                IssuerMetadata(
                    credentialAudience = rawIssuerMetadata["credential_issuer"] as String,
                    credentialEndpoint = credentialEndpoint,
                    credentialType = types,
                    context = context,
                    credentialFormat = CredentialFormat.LDP_VC,
                    authorizationServers = rawIssuerMetadata["authorization_servers"] as List<String>,
                    scope = "openid $scope"
                )
            }

            else -> throw IssuerMetadataFetchException("Unsupported or missing credential format in configuration")
        }
    }
}
