package io.mosip.vciclient.credential.response

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CredentialResponseTest {

    @Test
    fun `should deserialize and serialize v1 credential response`() {
        val responseBody =
            "{\"credentials\":[{\"credential\":\"LUpixVCWJk0eOt4CXQe1NXK.WZwmhmn9OQp6YxX0a2L\"}],\"credential_configuration_id\":\"UniversityDegreeCredential\",\"credential_issuer\":\"https://domain.net\"}"

        val credentialResponse: CredentialResponse =
            Gson().fromJson(responseBody, CredentialResponse::class.java)

        assertEquals(1, credentialResponse.credentials?.size)
        assertEquals("\"LUpixVCWJk0eOt4CXQe1NXK.WZwmhmn9OQp6YxX0a2L\"", credentialResponse.credentials?.get(0)?.credential.toString())
        assertEquals(
            "{\"credentials\":[{\"credential\":\"LUpixVCWJk0eOt4CXQe1NXK.WZwmhmn9OQp6YxX0a2L\"}],\"credential_configuration_id\":\"UniversityDegreeCredential\",\"credential_issuer\":\"https://domain.net\"}",
            credentialResponse.toJsonString()
        )
    }

    @Test
    fun `should deserialize v1 credential response with json object credential`() {
        val responseBody =
            "{\"credentials\":[{\"credential\":{\"@context\":[\"https://www.w3.org/2018/credentials/v1\"],\"type\":[\"VerifiableCredential\"]}}]}"

        val credentialResponse: CredentialResponse =
            Gson().fromJson(responseBody, CredentialResponse::class.java)

        assertEquals(1, credentialResponse.credentials?.size)
        val credentialElement = credentialResponse.credentials?.get(0)?.credential
        assert(credentialElement?.isJsonObject == true)
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

    @Test
    fun `should deserialize item with missing credential key as null`() {
        val responseBody = "{\"credentials\":[{}]}"

        val credentialResponse: CredentialResponse =
            Gson().fromJson(responseBody, CredentialResponse::class.java)

        assertEquals(1, credentialResponse.credentials?.size)
        assertNull(credentialResponse.credentials?.get(0)?.credential)
    }

    @Test
    fun `should deserialize null item in credentials array`() {
        val responseBody = "{\"credentials\":[null]}"

        val credentialResponse: CredentialResponse =
            Gson().fromJson(responseBody, CredentialResponse::class.java)

        assertEquals(1, credentialResponse.credentials?.size)
        assertNull(credentialResponse.credentials?.get(0))
    }
}
