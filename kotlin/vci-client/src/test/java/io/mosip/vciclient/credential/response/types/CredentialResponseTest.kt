package io.mosip.vciclient.credential.response.types

import com.google.gson.Gson
import io.mosip.vciclient.credential.response.CredentialResponse
import io.mosip.vciclient.credential.response.CredentialResponseDraft13
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CredentialResponseTest {

    @Test
    fun `should deserialize and serialize v1 credential response`() {
        val responseBody =
            "{\"credentials\":[{\"id\":\"https://domain.net/credentials/12345-87435\"}],\"credential_configuration_id\":\"UniversityDegreeCredential\",\"credential_issuer\":\"https://domain.net\"}"

        val credentialResponse: CredentialResponse =
            Gson().fromJson(responseBody, CredentialResponse::class.java)

        assertEquals(1, credentialResponse.credentials?.size)
        assertEquals(
            "{\"credentials\":[{\"id\":\"https://domain.net/credentials/12345-87435\"}],\"credential_configuration_id\":\"UniversityDegreeCredential\",\"credential_issuer\":\"https://domain.net\"}",
            credentialResponse.toJsonString()
        )
    }

    @Test
    fun `should deserialize and serialize draft13 credential response`() {
        val responseBody =
            "{\"credential\":\"omdkb2NUeXBl\",\"credentialConfigurationId\":\"org.iso.18013.5.1.mDL\",\"credentialIssuer\":\"https://domain.net\"}"

        val credentialResponse: CredentialResponseDraft13 =
            Gson().fromJson(responseBody, CredentialResponseDraft13::class.java)

        assertEquals("\"omdkb2NUeXBl\"", credentialResponse.credential.toString())
        assertEquals(
            "{\"credential\":\"omdkb2NUeXBl\",\"credentialConfigurationId\":\"org.iso.18013.5.1.mDL\",\"credentialIssuer\":\"https://domain.net\"}",
            credentialResponse.toJsonString()
        )
    }

    @Test
    fun `v1 response should not expose singular credential field`() {
        val response = CredentialResponse(credentials = emptyList())

        assertNull(Gson().fromJson(response.toJsonString(), Map::class.java)["credential"])
    }
}
