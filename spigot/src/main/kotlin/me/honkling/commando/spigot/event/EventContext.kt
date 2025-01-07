package me.honkling.commando.spigot.event

import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

data class EventContext(
    val function: KFunction<*>,
    val eventType: KClass<out Event>,
    val priority: EventPriority
)
