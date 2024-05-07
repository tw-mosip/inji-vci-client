export class InvalidAccessTokenError extends Error {
    constructor(message) {
        super(`Access token is invalid - ${message}`);
        this.name = 'InvalidAccessTokenError';
    }
}
