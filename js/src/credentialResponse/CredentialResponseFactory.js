const {CredentialFormat} = require("../constants/CredentialFormat");
const LdpVcCredentialResponse = require("./types/ldpVc/LdpVcCredentialResponse");

class CredentialResponseFactory {
    static createCredentialResponse(formatType, response) {
        switch (formatType) {
            case CredentialFormat.LDP_VC:
                return new LdpVcCredentialResponse().toJsonString(response);
            default:
                throw new Error('Unsupported credential format type');
        }
    }
}

module.exports = CredentialResponseFactory;