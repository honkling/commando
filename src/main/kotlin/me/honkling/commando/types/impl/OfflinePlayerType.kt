package me.honkling.commando.types.impl

import me.honkling.commando.types.MatchResult
import me.honkling.commando.types.Type
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender

object OfflinePlayerType : Type<OfflinePlayer> {
	override fun match(player: CommandSender, input: String): OfflinePlayer {
		return Bukkit.getOfflinePlayer(input.split(" ")[0])
	}

	override fun matches(player: CommandSender, input: String): MatchResult {
		return MatchResult(true, 1)
	}

	override fun complete(player: CommandSender, input: String): List<String> {
		return Bukkit
				.getOnlinePlayers()
				.map { it.name }
				.filter { it.contains(input.split(" ")[0]) }
	}
}