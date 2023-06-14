package me.honkling.commando.types

import org.bukkit.command.CommandSender

interface Type<T> {
    fun match(player: CommandSender, input: String): T
    fun matches(player: CommandSender, input: String): MatchResult
    fun complete(player: CommandSender, input: String): List<String>
}