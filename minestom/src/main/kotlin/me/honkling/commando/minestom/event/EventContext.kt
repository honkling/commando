package me.honkling.commando.minestom.event

import net.minestom.server.event.Event
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

data class EventContext(
    val function: KFunction<*>,
    val eventType: KClass<out Event>,
)
