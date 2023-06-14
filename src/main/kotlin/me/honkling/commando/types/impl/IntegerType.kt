package me.honkling.commando.types.impl

import me.honkling.commando.types.MatchResult
import me.honkling.commando.types.Type
import org.bukkit.command.CommandSender

object IntegerType : Type<Int> {
    override fun match(sender: CommandSender, input: String): Int {
        return input.split(" ")[0].toInt()
    }

    override fun matches(sender: CommandSender, input: String): MatchResult {
        return MatchResult(Regex("^\\d+(?!\\S)").containsMatchIn(input), 1)
    }

    override fun complete(sender: CommandSender, input: String): List<String> {
        return emptyList()
    }
}