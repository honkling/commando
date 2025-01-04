package me.honkling.commando.common.parser

import me.honkling.commando.common.Commando
import me.honkling.commando.common.exception.ParseError
import me.honkling.commando.common.node.Node
import me.honkling.commando.common.parser.handle.FunctionHandle
import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.kotlinFunction

data class ClassParseResult(
    val interactionType: InteractionType<*, *,  *>,
    val node: Node<*>
)

fun parseClass(commando: Commando, clazz: KClass<*>): Result<ClassParseResult> {
    println("Testing class ${clazz.qualifiedName}")

    for (interaction in commando.interactionRegistry.interactionTypes) {
        if (interaction.testParent(clazz).isFailure) {
            println("Test parent on class failed")
            continue
        }

        val node = interaction.createRootNode(clazz)
        println("Created node $node")

        for ((java, kotlin) in clazz.java.declaredMethods.mapNotNull {
            it to (it.kotlinFunction ?: return@mapNotNull null)
        }) {
            println("Testing function ${kotlin.name} ($java) ($kotlin)")
            println("${kotlin.returnType != Unit::class} (${kotlin.returnType} != ${Unit::class}) - ${!Modifier.isStatic(java.modifiers)}")
            println("${java.declaringClass.name}")

            if (kotlin.returnType != Unit::class.createType() || !Modifier.isStatic(java.modifiers))
                continue

            @Suppress("UNCHECKED_CAST")
            kotlin as KFunction<Unit>
            val handle = FunctionHandle(interaction, kotlin)
            val testResult = interaction.testFunction(clazz, handle)

            if (testResult.isFailure) {
                val exception = testResult.exceptionOrNull()!!
                commando.logger.warning("Skipping function '${handle.name}' of class '${clazz.qualifiedName}': ${exception.message}")
                continue
            }

            println("Parsing function")

            // Reflection for now because Kotlin is being really weird
            // about generics and I don't feel like figuring it out !!

            (interaction::parse as KFunction<Unit>).call(node, clazz, handle)
        }

        for (child in clazz.nestedClasses) {
            val parsedChild = parseClass(commando, child)

            if (parsedChild.isSuccess) {
                val childNode = parsedChild.getOrThrow().node
                childNode.parent = node
                node.children += childNode
            }
        }

        return Result.success(ClassParseResult(interaction, node))
    }

    return Result.failure(ParseError.NoCandidates("No interaction types matched."))
}