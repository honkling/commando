package me.honkling.commando.types.impl

import me.honkling.commando.types.MatchResult
import me.honkling.commando.types.Type

object IntegerType : Type<Int> {
    override fun match(input: String): Int {
        return input.split(" ")[0].toInt()
    }

    override fun matches(input: String): MatchResult {
        return MatchResult(Regex("^\\d+(?!\\S)").containsMatchIn(input), 1)
    }

    override fun complete(input: String): List<String> {
        return emptyList()
    }
}