package me.honkling.commando.types.impl

import me.honkling.commando.types.MatchResult
import me.honkling.commando.types.Type

object BooleanType : Type<Boolean> {
    override fun match(input: String): Boolean {
        return input.split(" ")[0].toBoolean()
    }

    override fun matches(input: String): MatchResult {
        return MatchResult(Regex("^(true|false)(?!\\S)", RegexOption.IGNORE_CASE).containsMatchIn(input), 1)
    }

    override fun complete(input: String): List<String> {
        return listOf("true", "false").filter { it.contains(input.lowercase().split(" ")[0]) }
    }
}