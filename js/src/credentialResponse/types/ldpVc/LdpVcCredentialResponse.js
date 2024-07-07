import { CredentialResponse } from "../../CredentialResponse";

export class LdpVcCredentialResponse extends CredentialResponse {

    toJsonString(response) {
        return JSON.parse(response);
    }
}