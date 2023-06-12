package me.honkling.commando.types.impl

import me.honkling.commando.types.MatchResult
import me.honkling.commando.types.Type
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object PlayerType : Type<Player> {
	override fun match(input: String): Player {
		return Bukkit.getPlayer(input.split(" ")[0])!!
	}

	override fun matches(input: String): MatchResult {
		return MatchResult(Bukkit.getPlayer(input.split(" ")[0]) != null, 1)
	}

	override fun complete(input: String): List<String> {
		return Bukkit
				.getOnlinePlayers()
				.map { it.name }
				.filter { it.contains(input.split(" ")[0]) }
	}
}