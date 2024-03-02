package me.honkling.commando.spigot.impl

import me.honkling.commando.common.generic.IPlugin
import org.bukkit.plugin.java.JavaPlugin

class Plugin(private val plugin: JavaPlugin) : IPlugin<JavaPlugin> {
    override fun get(): JavaPlugin = plugin

    override fun warn(message: String) {
        plugin.logger.warning(message)
    }

    override fun error(message: String) {
        plugin.logger.severe(message)
    }
}