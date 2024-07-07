import { ProofType } from "../../constants/ProofType";
import { CredentialRequest } from "../credentialRequest";

export class LdpVcCredentialRequest extends CredentialRequest {
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
            throw new Error('DownloadFailedError: requestGenerationFailed');
        }
    }
}

class CredentialRequestBody {
    constructor(format, credentialDefinition, proof) {
        this.format = format;
        this.credential_definition = credentialDefinition;
        this.proof = proof;
    }
    toJson() {
        return JSON.stringify(this);
    }
}

class CredentialDefinition {
    constructor(type) {
        this.type = type;
        this["@context"] = ["https://www.w3.org/2018/credentials/v1"];
    }
}