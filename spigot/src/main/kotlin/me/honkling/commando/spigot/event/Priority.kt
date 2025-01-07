package me.honkling.commando.spigot.event

import org.bukkit.event.EventPriority

@Target(AnnotationTarget.FUNCTION)
annotation class Priority(val priority: EventPriority)
