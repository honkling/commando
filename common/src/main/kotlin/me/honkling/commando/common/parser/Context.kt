package me.honkling.commando.common.parser

import me.honkling.commando.common.node.Node
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.javaMethod

typealias ContextCreatorBlock<Context, This> = (command: Node<*>, context: Context) -> This

abstract class ContextProvider
class ContextCreator<Context, This : ContextProvider>(
    val thisClass: KClass<This>,
    val block: ContextCreatorBlock<Context, This>
)

fun KFunction<Unit>.getContextProviderType(): KClass<out ContextProvider>? {
    val type = javaMethod!!.parameters.firstOrNull()?.type?.kotlin
        ?: return null

    @Suppress("UNCHECKED_CAST")
    if (type.isSubclassOf(ContextProvider::class))
        return type as KClass<out ContextProvider>

    return null
}