package me.honkling.commando.common.exception

open class ParseError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NotApplicable(message: String, cause: Throwable? = null) : ParseError(message, cause)
    class NoCandidates(message: String, cause: Throwable? = null) : ParseError(message, cause)
}