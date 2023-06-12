package me.honkling.commando.types.impl

import me.honkling.commando.types.MatchResult
import me.honkling.commando.types.Type
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer

object OfflinePlayerType : Type<OfflinePlayer> {
	override fun match(input: String): OfflinePlayer {
		return Bukkit.getOfflinePlayer(input.split(" ")[0])
	}

	override fun matches(input: String): MatchResult {
		return MatchResult(true, 1)
	}

	override fun complete(input: String): List<String> {
		return Bukkit
				.getOnlinePlayers()
				.map { it.name }
				.filter { it.contains(input.split(" ")[0]) }
	}
}