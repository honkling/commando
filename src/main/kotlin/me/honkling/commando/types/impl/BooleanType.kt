package me.honkling.commando.types.impl

import me.honkling.commando.types.MatchResult
import me.honkling.commando.types.Type
import org.bukkit.command.CommandSender

object BooleanType : Type<Boolean> {
    override fun match(player: CommandSender, input: String): Boolean {
        return input.split(" ")[0].toBoolean()
    }

    override fun matches(player: CommandSender, input: String): MatchResult {
        return MatchResult(Regex("^(true|false)(?!\\S)", RegexOption.IGNORE_CASE).containsMatchIn(input), 1)
    }

    override fun complete(player: CommandSender, input: String): List<String> {
        return listOf("true", "false").filter { it.contains(input.lowercase().split(" ")[0]) }
    }
}