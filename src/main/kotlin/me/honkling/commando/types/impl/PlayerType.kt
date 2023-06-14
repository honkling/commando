package me.honkling.commando.types.impl

import me.honkling.commando.types.MatchResult
import me.honkling.commando.types.Type
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object PlayerType : Type<Player> {
	override fun match(sender: CommandSender, input: String): Player {
		return Bukkit.getPlayer(input.split(" ")[0])!!
	}

	override fun matches(sender: CommandSender, input: String): MatchResult {
		return MatchResult(Bukkit.getPlayer(input.split(" ")[0]) != null, 1)
	}

	override fun complete(sender: CommandSender, input: String): List<String> {
		return Bukkit
				.getOnlinePlayers()
				.map { it.name }
				.filter { it.contains(input.split(" ")[0]) }
	}
}