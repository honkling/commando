package me.honkling.commando.minestom.command

@Target(AnnotationTarget.FILE, AnnotationTarget.CLASS)
annotation class Command(
    val name: String,
    vararg val aliases: String
)
