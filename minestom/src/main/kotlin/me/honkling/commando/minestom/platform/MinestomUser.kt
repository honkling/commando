package me.honkling.commando.minestom.platform

import me.honkling.commando.common.platform.User
import net.minestom.server.command.CommandSender
import net.minestom.server.command.ConsoleSender
import net.minestom.server.command.ServerSender
import net.minestom.server.entity.Player

class MinestomUser(accessor: CommandSender) : User<CommandSender>(accessor) {
    override val displayName =
        when (accessor) {
            is Player -> accessor.username
            is ConsoleSender -> "Console"
            is ServerSender -> "Server"
            else -> "Unknown"
        }
    override val identifier =
        when (accessor) {
            is Player -> accessor.uuid.toString()
            is ConsoleSender -> "<console>"
            is ServerSender -> "<server>"
            else -> "<unknown>"
        }

}