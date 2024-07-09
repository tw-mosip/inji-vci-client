class DownloadFailedException extends Error {
    constructor(message) {
        super(`Download failed - ${message}`);
        this.name = "DownloadFailedException";
    }
}

module.exports = DownloadFailedException;