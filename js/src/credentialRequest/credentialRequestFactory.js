import { LdpVcCredentialRequest } from "./types/LdpVcCredentialRquest";
import { CredentialFormat } from "../constants/CredentialFormat";

export class CredentialRequestFactory {
  static createCredentialRequest(
    credentialFormat,
    accessToken,
    issuerMetaData,
    proof
  ) {
    switch (credentialFormat) {
      case CredentialFormat.LDP_VC:
        return new LdpVcCredentialRequest(
          accessToken,
          issuerMetaData,
          proof
        ).constructRequest();
      default:
        throw new Error("Unsupported credential format");
    }
  }
}