export class Logger {
    static logTag = "INJI-VCI-Client : "

    static error(message) {
        console.error(`${(Logger.logTag)} ${message}`)
    }
}
