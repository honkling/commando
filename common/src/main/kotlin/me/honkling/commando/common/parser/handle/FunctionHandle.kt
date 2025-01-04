package me.honkling.commando.common.parser.handle

import me.honkling.commando.common.parser.InteractionType
import java.lang.reflect.Parameter
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

class FunctionHandle(
    val interactionType: InteractionType<*, *, *>,
    val reflector: KFunction<Unit>
) {
    val name = reflector.name
    val parameters = reflector.javaMethod!!.parameters.withIndex()
        .filter { (index, parameter) ->
            index != 0 || parameter.type.kotlin !in interactionType.contexts.keys
        }
        .map { (index, parameter) -> parameter to reflector.parameters[index] }
}