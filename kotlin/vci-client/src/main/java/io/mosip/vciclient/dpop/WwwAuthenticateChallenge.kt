package io.mosip.vciclient.dpop

internal data class WwwAuthenticateChallenge(
    val isDpop: Boolean,
    val error: String?,
) {
    companion object {
        private val errorRegex = Regex("""error\s*=\s*"([^"]+)"""", RegexOption.IGNORE_CASE)

        fun parse(headerValue: String?): WwwAuthenticateChallenge {
            if (headerValue.isNullOrBlank()) {
                return WwwAuthenticateChallenge(isDpop = false, error = null)
            }
            val isDpop = Regex("""(^|,|\s)DPoP(\s|$)""", RegexOption.IGNORE_CASE)
                .containsMatchIn(headerValue)
            val error = errorRegex.find(headerValue)?.groupValues?.get(1)
            return WwwAuthenticateChallenge(isDpop = isDpop, error = error)
        }
    }
}
