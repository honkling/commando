package me.honkling.commando.spigot

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.event.PacketEvent
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
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

        if (hasPacketEvents() && PacketEvents.getAPI() != null)
            setupPacketListeners()
    }

    private fun setupPacketListeners() {
        val api = PacketEvents.getAPI()

        if (!api.isLoaded || !api.isInitialized) {
            plugin.logger.warning("commando failed to setup packet listeners. This is because the plugin developer has registered commando before initializing PacketEvents, or PacketEvents is broken.")
            return
        }


    }

    private fun hasPacketEvents(): Boolean {
        try {
            Class.forName("com.github.retrooper.packetevents.PacketEvents")
            return true
        } catch (_: ClassNotFoundException) {
            return false
        }
    }
}
