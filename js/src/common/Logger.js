class Logger {
  static logTag = "INJI-VCI-Client : ";

  static getLogTag(className, traceId) {
    return `INJI-VCI-Client : ${className} | traceID ${traceId}`;
  }

  static error(message, traceId) {
    console.error(`${Logger.logTag} ${message} | traceID ${traceId}`);
  }

  static warn(message, traceId) {
    console.warn(`${Logger.logTag} ${message} | traceID ${traceId}`);
  }
}

module.exports = Logger;