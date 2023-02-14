package me.honkling.commando.types.impl

import me.honkling.commando.types.Type
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer

object OfflinePlayerType : Type<OfflinePlayer> {
	override fun match(input: String): OfflinePlayer {
		return Bukkit.getOfflinePlayer(input)
	}

	override fun matches(input: String): Boolean {
		return true
	}

	override fun complete(input: String): List<String> {
		return Bukkit
				.getOnlinePlayers()
				.map { it.name }
				.filter { it.contains(input) }
	}
}