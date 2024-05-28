import {requestCredential} from "../src/VCIClient";
import {IssuerMeta} from "../src/dto/IssuerMeta";
import {InvalidAccessTokenError} from "../src/error/InvalidAccessTokenError";
// var pem2jwk = require('pem-jwk').pem2jwk
import {pem2jwk} from "pem-jwk"
var jwk2pem = require('pem-jwk').jwk2pem

// jest.mock("jwt-decode")
describe('VCI Client test', () => {

    const credentialEndpoint = "https://domain.net/credential"
    const credentialAudience = "https://domain.net"
    const downloadTimeoutInMillSeconds = 3000;
    const credentialType = ["VerifiableCredential"];
    const credentialFormat = "ldp_vc";
    const issuerMeta = new IssuerMeta(credentialAudience, credentialEndpoint, downloadTimeoutInMillSeconds, credentialType, credentialFormat);

    function signer() {
        //Logic for signing
    }

    const accessToken = "accessToken";
    const publicKeyPem = "accessToken";
    it.skip('should  return credential when valid access token, public key PEM is passed and credential endpoint api is success', () => {
            requestCredential(issuerMeta, signer, accessToken, publicKeyPem)
    });

    it.only('should make api call to credential endpoint with the right params', () => {
        const str = "-----BEGIN RSA PUBLIC KEY-----\n" +
            "MIICCgKCAgEA3PXjIIoB/Aupxs1mMKH7zOth4XV5S4r1KiEsUla027jMYc2NP6uw\n" +
            "nUjoYjLEMMtsWRUpFWpN1nMYG2htwfjZDr5X6lMd+c+U2XaEaoDy8YwRFWBmEegY\n" +
            "Jx3hlwfMmIas9mBr4jn0pqmDUTz1FGBsSVXOizg6u5HPmxMO09ksQY/jiaUcP8rn\n" +
            "M96yeU1Dr/ZcxnvBx2QPU4ZT5vlAJgc0peEHCaNeJy+abSOmsT6V6J1KpvccMD9A\n" +
            "qkD6hFXkoPRI0yUBkpWB1Bub/nxRQeZyZjOBIoAjhocxxOO52z1BIT9kb2J5C2CC\n" +
            "GrETK10x11F0tZj1fdtXl1gJBhtvrXjUwoqmJ6gqJwRhnvMl4anSKJXIQYAtc/ay\n" +
            "86kp//QN07rNvtA7joU1LVHoXJFvzwXCtXcob7zgno2uj6XQvMmt7WO0uycqJ0o+\n" +
            "MTvmw6AIvWl6G9FwQndbTNtScMC5lSTouUIeJUzc9A4ukK6Pu5Vze0xjHLOtWH2g\n" +
            "YmEGeolfC/u4QFEJxDQS5/XBhfx5vg0sEQiupV3Pn15agTPdf6m1VQrEjuE97W+t\n" +
            "YlYhZUymt7Ffs3A8IEHPRaIEqL9ZdBTkA46H+qDbDtiACmtjRuG5LuXoGVmzgGDC\n" +
            "to4v0e+wZhZz3IordrsVLJakEBJyPEL6Orhl9aQ38nCt5+byB7g2f48CAwEAAQ==\n" +
            "-----END RSA PUBLIC KEY-----"
        var jwk = pem2jwk(str)
        var pem = jwk2pem(jwk)
        console.log("jwk ",jwk)
       expect(pem).toBe(str)
    });

    it.skip('should throw invalid access token error when invalid access token is passed', () => {

        // expect(requestCredential(issuerMeta, signer, accessToken, publicKeyPem)).toThrow(new InvalidAccessTokenError("Invalid token specified: missing part #2"))
    });
});
