package me.honkling.commando.spigot.command

const val defaultDescription = "No description provided"

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FILE, AnnotationTarget.CLASS)
annotation class Command(
    val name: String,
    vararg val aliases: String,
    val description: String = defaultDescription,
    /**
     * The permission of the command.
     * An empty string is treated as no permission.
     */
    val permission: String = ""
)
