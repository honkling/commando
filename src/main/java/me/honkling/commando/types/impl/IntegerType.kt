package me.honkling.commando.types.impl

import me.honkling.commando.types.Type

object IntegerType : Type<Int> {
    override fun match(input: String): Int {
        return input.toInt()
    }

    override fun matches(input: String): Boolean {
        return input.matches(Regex("\\d+"))
    }

    override fun complete(input: String): List<String> {
        return emptyList()
    }
}