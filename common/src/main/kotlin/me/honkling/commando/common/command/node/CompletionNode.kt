package me.honkling.commando.common.command.node

import me.honkling.commando.common.node.AutoCompletable
import me.honkling.commando.common.node.Node
import me.honkling.commando.common.parser.handle.FunctionHandle
import me.honkling.commando.common.platform.User
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.isAccessible

class CompletionNode<Anno : Annotation>(
    parent: CommandNode<Anno>,
    name: String,
    val handle: FunctionHandle
) : Node<Nothing?>(parent, name, null) {
    fun autoComplete(user: User<*>, node: ParameterNode<Anno>, input: String): List<String> {
        @Suppress("UNCHECKED_CAST")
        val function = handle.reflector as KFunction<List<String>>
        val accessorType = handle.parameters.first().first.type

        println("Auto completing ${handle.name} (${accessorType.name}) (${user.accessor::class.java.name})")

        if (!accessorType.isAssignableFrom(user.accessor::class.java))
            return emptyList()

        function.isAccessible = true
        return function.call(user.accessor, node, input)
    }

    override fun toString(): String {
        return "Completion${super.toString()}"
    }
}