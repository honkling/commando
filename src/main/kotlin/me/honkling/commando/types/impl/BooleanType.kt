package me.honkling.commando.types.impl

import me.honkling.commando.types.Type

object BooleanType : Type<Boolean> {
    override fun match(input: String): Boolean {
        return input.toBoolean()
    }

    override fun matches(input: String): Boolean {
        return input.matches(Regex("(true|false)", RegexOption.IGNORE_CASE))
    }

    override fun complete(input: String): List<String> {
        return listOf("true", "false").filter { it.contains(input.lowercase()) }
    }

}