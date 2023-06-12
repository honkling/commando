package me.honkling.commando.types

data class MatchResult(val matches: Boolean, val size: Int = -1) {
    init {
        if (matches && size < 1)
            throw IllegalStateException("Bad match result")
    }
}
