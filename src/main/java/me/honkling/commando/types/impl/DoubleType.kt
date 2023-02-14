package me.honkling.commando.types.impl

import me.honkling.commando.types.Type

object DoubleType : Type<Double> {
    override fun match(input: String): Double {
        return input.toDouble()
    }

    override fun matches(input: String): Boolean {
        return input.matches(Regex("\\d+\\.\\d+"))
    }

    override fun complete(input: String): List<String> {
        return emptyList()
    }
}