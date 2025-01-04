package me.honkling.commando.common.exception

open class ParseError(message: String) : Exception(message) {
    class NotApplicable(message: String) : ParseError(message)
    class NoCandidates(message: String) : ParseError(message)
}