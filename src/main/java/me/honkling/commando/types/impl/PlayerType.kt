package me.honkling.commando.types.impl

import me.honkling.commando.types.Type
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object PlayerType : Type<Player> {
	override fun match(input: String): Player {
		return Bukkit.getPlayer(input)!!
	}

	override fun matches(input: String): Boolean {
		return Bukkit.getPlayer(input) != null
	}

	override fun complete(input: String): List<String> {
		return Bukkit
				.getOnlinePlayers()
				.map { it.name }
				.filter { it.contains(input) }
	}
}