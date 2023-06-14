package me.honkling.commando.types

import org.bukkit.command.CommandSender

interface Type<T> {
    fun match(sender: CommandSender, input: String): T
    fun matches(sender: CommandSender, input: String): MatchResult
    fun complete(sender: CommandSender, input: String): List<String>
}