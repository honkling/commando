package me.honkling.commando.spigot.event

import me.honkling.commando.spigot.SpigotCommando
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class UserEvents(private val commando: SpigotCommando) : Listener {
    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val userManager = commando.userManager
        userManager.deleteUser(event.player)
    }
}