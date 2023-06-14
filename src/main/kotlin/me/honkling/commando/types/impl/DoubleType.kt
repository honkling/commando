package me.honkling.commando.types.impl

import me.honkling.commando.types.MatchResult
import me.honkling.commando.types.Type
import org.bukkit.command.CommandSender

object DoubleType : Type<Double> {
    override fun match(sender: CommandSender, input: String): Double {
        return input.split(" ")[0].toDouble()
    }

    override fun matches(sender: CommandSender, input: String): MatchResult {
        return MatchResult(Regex("^\\d+\\.\\d+(?!\\S)").containsMatchIn(input), 1)
    }

    override fun complete(sender: CommandSender, input: String): List<String> {
        return emptyList()
    }
}