class IssuerMetaData {
    #credentialAudience;
    #credentialEndpoint;
    #downloadTimeoutInMillSeconds;
    #credentialType;
    #credentialFormat;

    /**
     * @param {string} credentialAudience
     * @param {string} credentialEndpoint
     * @param {number} downloadTimeoutInMillSeconds
     * @param {[string]} credentialType
     * @param {string} credentialFormat
     */
    constructor(credentialAudience, credentialEndpoint, downloadTimeoutInMillSeconds, credentialType, credentialFormat) {
        this.#credentialAudience = credentialAudience;
        this.#credentialEndpoint = credentialEndpoint;
        this.#downloadTimeoutInMillSeconds = downloadTimeoutInMillSeconds;
        this.#credentialType = credentialType;
        this.#credentialFormat = credentialFormat;
    }

    get credentialAudience() {
        return this.#credentialAudience;
    }

    get credentialEndpoint() {
        return this.#credentialEndpoint;
    }

    get downloadTimeoutInMillSeconds() {
        return this.#downloadTimeoutInMillSeconds;
    }

    get credentialType() {
        return this.#credentialType;
    }

    get credentialFormat() {
        return this.#credentialFormat;
    }
}

module.exports = IssuerMetaData;