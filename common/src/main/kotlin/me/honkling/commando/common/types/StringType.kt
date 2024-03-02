package me.honkling.commando.common.types

import me.honkling.commando.common.generic.ICommandSender

object StringType : Type<String> {
    private val regex = Regex("^(\"([^\"]|\\\\\")*\"|\\S+)")

    override fun validate(sender: ICommandSender<*>, input: String): Boolean {
        return regex.containsMatchIn(input)
    }

    override fun parse(sender: ICommandSender<*>, input: String): Pair<String, Int> {
        val match = regex.find(input)!!.value
        val size = match.split(" ").size

        if (match.startsWith("\"") && match.endsWith("\""))
            return match
                    .substring(1, match.length - 1)
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\") to size

        return match to size
    }

    override fun complete(sender: ICommandSender<*>, input: String): List<String> {
        return emptyList()
    }
}