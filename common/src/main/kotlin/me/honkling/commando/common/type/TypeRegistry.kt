package me.honkling.commando.common.type

import kotlin.reflect.KClass

//import me.honkling.commando.common.CommandManager

class TypeRegistry {
    private val internalMap = mutableMapOf<KClass<*>, Type<*>>()

    fun register(type: Type<*>, vararg classes: KClass<*>) {
        classes.forEach { internalMap[it] = type }
    }

    operator fun get(clazz: KClass<*>)
        = internalMap[clazz]

    operator fun contains(clazz: KClass<*>)
        = clazz in internalMap
}