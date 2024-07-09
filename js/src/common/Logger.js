class Logger {
  static logTag = "INJI-VCI-Client : ";

  static getLogTag(className, traceId) {
    return `INJI-VCI-Client : ${className} | traceID ${traceId}`;
  }

  static error(message) {
    console.error(`${Logger.logTag} ${message}`);
  }

  static warn(message) {
    console.warn(`${Logger.logTag} ${message}`);
  }
}

module.exports = Logger;
