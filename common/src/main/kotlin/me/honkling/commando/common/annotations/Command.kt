package me.honkling.commando.common.annotations

@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Retention(AnnotationRetention.RUNTIME)
annotation class Command(
        val name: String,
        vararg val aliases: String,
		val description: String = "A Warden command.",
		val usage: String = "<gray>Please use <white>/{0} help</white>.",
		val permission: String = "",
		val permissionMessage: String = "<gray>You don't have access to <white>/{0}</white>."
)
