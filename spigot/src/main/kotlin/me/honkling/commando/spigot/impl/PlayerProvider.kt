package me.honkling.commando.spigot.impl

import me.honkling.commando.common.generic.IPlayer
import org.bukkit.entity.Player

class PlayerProvider(private val player: Player) : IPlayer<Player> {
    override fun get(): Player {
        return player
    }
}