const CredentialResponse = require("../../CredentialResponse");

class LdpVcCredentialResponse extends CredentialResponse {

    toJson(response) {
        return JSON.parse(response);
    }
}

module.exports = LdpVcCredentialResponse;