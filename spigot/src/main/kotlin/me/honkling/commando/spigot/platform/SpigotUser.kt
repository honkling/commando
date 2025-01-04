package me.honkling.commando.spigot.platform

import me.honkling.commando.common.platform.User
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

class SpigotUser(accessor: CommandSender) : User<CommandSender>(accessor) {
    override val displayName =
        when (accessor) {
            is Player -> accessor.name
            is ConsoleCommandSender -> "Console"
            else -> "Unknown"
        }
    override val identifier =
        when (accessor) {
            is Player -> accessor.uniqueId.toString()
            is ConsoleCommandSender -> "<console>"
            else -> "<unknown>"
        }
}