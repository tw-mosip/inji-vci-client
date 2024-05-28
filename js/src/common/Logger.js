export class Logger {
    static logTag = "INJI-VCI-Client : "
    static traceabilityId = "<traceabilityId>"

    static error(message) {
        console.error(`${(Logger.logTag)}-${this.traceabilityId} ${message}`)
    }
}
