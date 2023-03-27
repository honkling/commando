package me.honkling.commando.lib

@Target(AnnotationTarget.FILE, AnnotationTarget.CLASS)
annotation class Command(
	val name: String,
	vararg val aliases: String,
	val description: String = "A Commando command.",
	val usage: String = "Invalid usage. Please check /{0} help.",
	val permission: String = "commando.{0}",
	val permissionMessage: String = "You don't have permission to do that."
)