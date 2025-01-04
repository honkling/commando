package me.honkling.commando.spigot

import me.honkling.commando.common.Commando
import me.honkling.commando.spigot.command.CommandInteraction
import me.honkling.commando.spigot.event.EventInteraction
import me.honkling.commando.spigot.platform.SpigotUserManager
import org.bukkit.plugin.java.JavaPlugin

class SpigotCommando(
    val plugin: JavaPlugin
) : Commando() {
    override val userManager = SpigotUserManager()

    init {
        interactionRegistry.register(CommandInteraction(this))
        interactionRegistry.register(EventInteraction(this))
    }
}
