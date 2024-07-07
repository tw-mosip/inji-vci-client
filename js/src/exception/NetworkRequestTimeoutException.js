export class NetworkRequestTimeoutException extends Error {
    constructor(message) {
      super(`Download failure occurred due to Network request timeout, details -  ${message}`);
      this.name = 'NetworkRequestTimeoutException';
    }
  }
  