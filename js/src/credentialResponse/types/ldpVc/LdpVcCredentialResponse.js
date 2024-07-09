const CredentialResponse = require("../../CredentialResponse");

class LdpVcCredentialResponse extends CredentialResponse {

    toJsonString(response) {
        return JSON.parse(response);
    }
}

module.exports = LdpVcCredentialResponse;