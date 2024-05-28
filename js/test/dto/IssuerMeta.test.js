import {IssuerMeta} from "../../src/dto/IssuerMeta";

describe('Issuer meta test', () => {
    it('should be able to create issuer meta object and access its fields', () => {
        const credentialEndpoint = "https://domain.net/credential"
        const credentialAudience = "https://domain.net"
        const downloadTimeoutInMillSeconds = 3000;
        const credentialType = ["VerifiableCredential"];
        const credentialFormat = "ldp_vc";
        const issuerMeta = new IssuerMeta(credentialAudience, credentialEndpoint, downloadTimeoutInMillSeconds, credentialType, credentialFormat);

        expect(issuerMeta.credentialAudience).toBe(credentialAudience)
        expect(issuerMeta.credentialEndpoint).toBe(credentialEndpoint)
        expect(issuerMeta.downloadTimeoutInMillSeconds).toBe(downloadTimeoutInMillSeconds)
        expect(issuerMeta.credentialType).toBe(credentialType)
        expect(issuerMeta.credentialFormat).toBe(credentialFormat)
    });
});
