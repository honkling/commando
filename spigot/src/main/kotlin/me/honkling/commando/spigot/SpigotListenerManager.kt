package me.honkling.commando.spigot

import me.honkling.commando.spigot.impl.Plugin
import me.honkling.knockffa.manager.ListenerManager
import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.plugin.EventExecutor
import org.bukkit.plugin.java.JavaPlugin

class SpigotListenerManager(plugin: JavaPlugin) : ListenerManager<JavaPlugin>(Plugin(plugin)) {
    override fun registerClass(clazz: Class<*>) {
        val listenerInstance = object : Listener {} as Listener

        for (method in clazz.declaredMethods) {
            val event = method.parameters.firstOrNull() ?: continue

            if (!method.canAccess(null) || !isEvent(event.type))
                continue

            @Suppress("UNCHECKED_CAST")
            Bukkit.getPluginManager().registerEvent(
                event.type as Class<out Event>,
                listenerInstance,
                EventPriority.NORMAL,
                EventExecutor { _, evt ->
                    method.invoke(null, evt)
                },
                plugin.get()
            )
        }
    }

    override fun isEvent(clazz: Class<*>): Boolean {
        return Event::class.java.isAssignableFrom(clazz)
    }
}