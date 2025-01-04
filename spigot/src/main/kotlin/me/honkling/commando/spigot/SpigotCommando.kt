package me.honkling.commando.spigot

import me.honkling.commando.common.Commando
import me.honkling.commando.common.platform.UserManager
import me.honkling.commando.spigot.command.SpigotCommandInteraction
import me.honkling.commando.spigot.platform.SpigotUserManager
import org.bukkit.plugin.java.JavaPlugin

class SpigotCommando(
    val plugin: JavaPlugin
) : Commando() {
    override val userManager = SpigotUserManager()

    init {
        interactionRegistry.register(SpigotCommandInteraction(this))
    }
}
