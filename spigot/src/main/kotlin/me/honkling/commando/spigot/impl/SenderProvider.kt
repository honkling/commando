package me.honkling.commando.spigot.impl

import me.honkling.commando.common.generic.ICommandSender
import org.bukkit.command.CommandSender

class SenderProvider(private val sender: CommandSender) : ICommandSender<CommandSender> {
    override fun get(): CommandSender {
        return sender
    }
}