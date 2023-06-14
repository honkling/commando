package me.honkling.commando.types.impl

import me.honkling.commando.types.MatchResult
import me.honkling.commando.types.Type
import org.bukkit.command.CommandSender

object DoubleType : Type<Double> {
    override fun match(player: CommandSender, input: String): Double {
        return input.split(" ")[0].toDouble()
    }

    override fun matches(player: CommandSender, input: String): MatchResult {
        return MatchResult(Regex("^\\d+\\.\\d+(?!\\S)").containsMatchIn(input), 1)
    }

    override fun complete(player: CommandSender, input: String): List<String> {
        return emptyList()
    }
}