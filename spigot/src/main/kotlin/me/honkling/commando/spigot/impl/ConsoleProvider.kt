package me.honkling.commando.spigot.impl

import me.honkling.commando.common.generic.IConsoleSender
import org.bukkit.Bukkit
import org.bukkit.command.ConsoleCommandSender

class ConsoleProvider : IConsoleSender<ConsoleCommandSender> {
    override fun get(): ConsoleCommandSender {
        return Bukkit.getConsoleSender()
    }
}