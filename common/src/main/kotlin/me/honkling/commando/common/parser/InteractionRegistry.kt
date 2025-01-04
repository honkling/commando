package me.honkling.commando.common.parser

class InteractionRegistry {
    val interactionTypes = mutableListOf<InteractionType<*, *, *>>()

    inline fun <reified T : InteractionType<*, *, *>> register(instance: T) {
        if (!contains<T>())
            interactionTypes += instance
    }

    inline fun <reified T : InteractionType<*, *, *>> get()
            = interactionTypes.find { it is T }

    inline fun <reified T : InteractionType<*, *, *>> contains()
            = interactionTypes.any { it is T }
}