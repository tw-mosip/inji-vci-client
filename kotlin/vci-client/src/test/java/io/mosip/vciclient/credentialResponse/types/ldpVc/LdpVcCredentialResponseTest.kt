package io.mosip.vciclient.credentialResponse.types.ldpVc

import com.google.gson.Gson
import org.junit.Assert.*

import org.junit.Test

class LdpVcCredentialResponseTest {

    @Test
    fun `should return json string of the LdpVc credential response`() {
        val credentialResponse =
            "{\"format\":\"ldp_vc\",\"credential\":{\"id\":\"https://domain.net/credentials/12345-87435\",\"type\":[\"VerifiableCredential\"],\"proof\":{\"type\":\"RsaSignature2018\",\"created\":\"2024-04-14T16:04:35Z\",\"proofValue\":\"23sx\",\"proofPurpose\":\"assertionMethod\",\"verificationMethod\":\"https://domain.net/.well-known/public-key.json\",\"jws\":\"eyJiweyrtwegrfwwaBKCGSwxjpa5suaMtgnQ\"},\"issuer\":\"https://domain.net/.well-known/issuer.json\",\"@context\":[\"https://www.w3.org/2018/credentials/v1\",\"https://domain.net/.well-known/context.json\",{\"sec\":\"https://w3id.org/security#\"}],\"issuanceDate\":\"2024-04-14T16:04:35.304Z\",\"expirationDate\":\"2024-07-28T11:41:43.216Z\",\"credentialSubject\":{\"face\":\"data:image/jpeg;base64,/9j/goKCyuig\",\"dateOfBirth\":\"2000/01/01\",\"id\":\"did:jwk:eyJr80435\",\"UIN\":\"9012378996\",\"email\":\"mockuser@gmail.com\"}}}"
        val ldpVcCredentialResponse: LdpVcCredentialResponse =
            Gson().fromJson(credentialResponse, LdpVcCredentialResponse::class.java)

        assertEquals(
            credentialResponse,
            ldpVcCredentialResponse.toJsonString()
        )
    }
}