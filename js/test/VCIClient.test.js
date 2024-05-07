import { requestCredential } from "../src/VCIClient";
import { IssuerMeta } from "../src/dto/IssuerMeta";
import { InvalidAccessTokenError } from "../src/error/InvalidAccessTokenError";
// var pem2jwk = require('pem-jwk').pem2jwk
import { pem2jwk } from "pem-jwk"
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
    const publicKeyPem = "publicKey";

    it.todo('should  return credential when valid access token, public key PEM is passed and credential endpoint api is success');

    it.todo('should throw invalid access token error when invalid access token is passed');
});
