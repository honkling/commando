package me.honkling.commando.spigot.types

import me.honkling.commando.common.generic.ICommandSender
import me.honkling.commando.common.types.Type
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer

object OfflinePlayerType : Type<OfflinePlayer> {
    override fun validate(sender: ICommandSender<*>, input: String): Boolean {
        return true
    }

    override fun parse(sender: ICommandSender<*>, input: String): Pair<OfflinePlayer, Int> {
        return Bukkit.getOfflinePlayer(input.split(" ")[0]) to 1
    }

    override fun complete(sender: ICommandSender<*>, input: String): List<String> {
        return Bukkit
                .getOnlinePlayers()
                .map { it.name }
                .filter { it.contains(input.split(" ")[0]) }
    }
}