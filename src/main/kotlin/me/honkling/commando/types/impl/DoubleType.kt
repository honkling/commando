package me.honkling.commando.types.impl

import me.honkling.commando.types.MatchResult
import me.honkling.commando.types.Type

object DoubleType : Type<Double> {
    override fun match(input: String): Double {
        return input.split(" ")[0].toDouble()
    }

    override fun matches(input: String): MatchResult {
        return MatchResult(Regex("^\\d+\\.\\d+(?!\\S)").containsMatchIn(input), 1)
    }

    override fun complete(input: String): List<String> {
        return emptyList()
    }
}