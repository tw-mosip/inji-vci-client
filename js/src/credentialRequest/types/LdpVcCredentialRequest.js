const Logger = require("../../common/Logger");
const {ProofType} = require("../../constants/ProofType");
const DownloadFailedException = require("../../exception/DownloadFailedException");
const CredentialRequest = require("../credentialRequest");

class LdpVcCredentialRequest extends CredentialRequest {
    constructor(accessToken, issuerMetaData, proof) {
        super();
        this.accessToken = accessToken;
        this.issuerMetaData = issuerMetaData;
        this.proof = proof;
    }

    constructRequest() {
        const request = {
          requestBody: this.generateRequestBody(),
          requestHeader: {
            'Authorization': `Bearer ${this.accessToken}`,
            'Content-Type': 'application/json',
          },
        };
        return request;
      }

    generateRequestBody() {
        try {
            const requestBody = new CredentialRequestBody(
              this.issuerMetaData.credentialFormat,
              new CredentialDefinition(this.issuerMetaData.credentialType),
              {
                proof_type: ProofType.JWT,
                jwt: this.proof,
              },
            ).toJson();
            return requestBody;
        } catch (error) {
            const errorMessage = "RequestGenerationFailed";
            Logger.error(errorMessage);
            throw new DownloadFailedException(errorMessage);
        }
    }
}

class CredentialRequestBody {
    constructor(format, credentialDefinition, proof) {
        this.format = format;
        this.credential_definition = credentialDefinition;
        this.proof = proof;
    }
    toJsonString() {
        return JSON.stringify(this);
    }
}

class CredentialDefinition {
    constructor(type) {
        this.type = type;
        this["@context"] = ["https://www.w3.org/2018/credentials/v1"];
    }
}

module.exports = LdpVcCredentialRequest;