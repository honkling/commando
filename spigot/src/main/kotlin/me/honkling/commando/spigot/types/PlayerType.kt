package me.honkling.commando.spigot.types

import me.honkling.commando.common.generic.ICommandSender
import me.honkling.commando.common.types.Type
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer

object PlayerType : Type<OfflinePlayer> {
    override fun validate(sender: ICommandSender<*>, input: String): Boolean {
        return Bukkit.getPlayerExact(input.split(" ")[0]) != null
    }

    override fun parse(sender: ICommandSender<*>, input: String): Pair<OfflinePlayer, Int> {
        return Bukkit.getPlayerExact(input.split(" ")[0])!! to 1
    }

    override fun complete(sender: ICommandSender<*>, input: String): List<String> {
        return Bukkit
                .getOnlinePlayers()
                .map { it.name }
                .filter { input.split(" ")[0].lowercase() in it.lowercase() }
    }
}