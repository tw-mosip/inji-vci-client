import { CredentialFormat } from "../constants/CredentialFormat";
import { LdpVcCredentialResponse } from "./types/ldpVc/LdpVcCredentialResponse";

export class CredentialResponseFactory {
    static createCredentialResponse(formatType, response) {
        switch (formatType) {
            case CredentialFormat.LDP_VC:
                return new LdpVcCredentialResponse().toJsonString(response);
            default:
                throw new Error('Unsupported credential format type');
        }
    }
}