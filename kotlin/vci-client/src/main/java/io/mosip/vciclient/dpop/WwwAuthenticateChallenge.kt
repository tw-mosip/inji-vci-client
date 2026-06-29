package io.mosip.vciclient.dpop

internal data class WwwAuthenticateChallenge(
    val isDpop: Boolean,
    val isBearer: Boolean,
    val error: String?,
) {
    companion object {
        private val errorRegex = Regex("""error\s*=\s*"([^"]+)"""", RegexOption.IGNORE_CASE)
        private val dpopRegex = Regex("""(^|,|\s)DPoP(\s|$)""", RegexOption.IGNORE_CASE)
        private val bearerRegex = Regex("""(^|,|\s)Bearer(\s|$)""", RegexOption.IGNORE_CASE)

        fun parse(headerValue: String?): WwwAuthenticateChallenge {
            if (headerValue.isNullOrBlank()) {
                return WwwAuthenticateChallenge(isDpop = false, isBearer = false, error = null)
            }
            val isDpop = dpopRegex.containsMatchIn(headerValue)
            val isBearer = bearerRegex.containsMatchIn(headerValue)
            val error = errorRegex.find(headerValue)?.groupValues?.get(1)
            return WwwAuthenticateChallenge(isDpop = isDpop, isBearer = isBearer, error = error)
        }
    }
}
