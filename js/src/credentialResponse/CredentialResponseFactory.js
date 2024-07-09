const {CredentialFormat} = require("../constants/CredentialFormat");
const DownloadFailedException = require("../exception/DownloadFailedException");
const LdpVcCredentialResponse = require("./types/ldpVc/LdpVcCredentialResponse");

class CredentialResponseFactory {
    static createCredentialResponse(formatType, response) {
        switch (formatType) {
            case CredentialFormat.LDP_VC:
                return new LdpVcCredentialResponse().toJson(response);
            default:
                const errorMessage = `Unsupported credential format type: ${formatType}`;
                Logger.error(errorMessage);
                throw new DownloadFailedException(errorMessage);
        }
    }
}

module.exports = CredentialResponseFactory;