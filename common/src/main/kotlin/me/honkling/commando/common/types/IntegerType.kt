package me.honkling.commando.common.types

import me.honkling.commando.common.generic.ICommandSender

object IntegerType : Type<Int> {
    override fun validate(sender: ICommandSender<*>, input: String): Boolean {
        val regex = Regex("^\\d+(?!\\S)")
        return regex.containsMatchIn(input)
    }

    override fun parse(sender: ICommandSender<*>, input: String): Pair<Int, Int> {
        return input.split(" ")[0].toInt() to 1
    }

    override fun complete(sender: ICommandSender<*>, input: String): List<String> {
        return emptyList()
    }
}