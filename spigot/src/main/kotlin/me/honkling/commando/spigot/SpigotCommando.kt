package me.honkling.commando.spigot

import me.honkling.commando.common.Commando
import me.honkling.commando.spigot.command.CommandInteraction
import me.honkling.commando.spigot.event.EventInteraction
import me.honkling.commando.spigot.platform.SpigotUserManager
import me.honkling.commando.spigot.type.OfflinePlayerType
import me.honkling.commando.spigot.type.PlayerType
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class SpigotCommando(
    val plugin: JavaPlugin
) : Commando(plugin::class) {
    override val userManager = SpigotUserManager()

    init {
        interactionRegistry.register(CommandInteraction(this))
        interactionRegistry.register(EventInteraction(this))

        typeRegistry.register(PlayerType(), Player::class)
        typeRegistry.register(OfflinePlayerType(), OfflinePlayer::class)
    }
}
