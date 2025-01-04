package me.honkling.commando.common.type

import me.honkling.commando.common.node.Node
import me.honkling.commando.common.platform.User

class BooleanType : Type<Boolean>() {
    override fun parse(user: User<*>, node: Node<*>, input: String, autoCompleting: Boolean): Result<Pair<Boolean, String>> {
        val first = input(input, 1)

        return Result.success(when (first) {
            "true", "yes", "on" -> true
            "false", "no", "off" -> false
            else -> {
                val display = if (first.isEmpty()) "nothing" else "'$first'"
                return Result.failure(IllegalArgumentException("Expected one of true/false/yes/no/on/off, found $display"))
            }
        } to input.substringAfter(' ', ""))
    }

    override fun suggest(user: User<*>, node: Node<*>, input: String): List<String> {
        val first = input(input, 1)
        return listOf("true", "yes", "on", "false", "no", "off")
            .filter { first in it }
    }
}