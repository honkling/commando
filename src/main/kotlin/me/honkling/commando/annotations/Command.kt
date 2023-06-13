package me.honkling.commando.annotations

@Target(AnnotationTarget.FILE, AnnotationTarget.CLASS)
annotation class Command(
    val name: String,
    vararg val aliases: String,
    val description: String = "A Commando command.",
    val usage: String = "Invalid usage.",
    val permission: String = "",
    val permissionMessage: String = "You don't have permission to do that."
)
