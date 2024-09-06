class Logger {
  static logTag = "INJI-VCI-Client:";
  static traceId = "";

  static setLogTag(traceId, className) {
    Logger.logTag = `INJI-VCI-Client: ${className} | traceID: ${traceId} |`;
  }

  static getLogTag() {
    return Logger.logTag;
  }

  static error(message) {
    console.error(`${Logger.logTag} ${message}`);
  }

  static warn(message) {
    console.warn(`${Logger.logTag} ${message}`);
  }
}

module.exports = Logger;