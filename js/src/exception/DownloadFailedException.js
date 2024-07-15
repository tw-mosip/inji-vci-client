class DownloadFailedException extends Error {
    constructor(message) {
        super(`${message}`);
        this.name = "DownloadFailedException";
    }
}

module.exports = DownloadFailedException;