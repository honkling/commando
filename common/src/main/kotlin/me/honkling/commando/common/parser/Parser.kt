package me.honkling.commando.common.parser

import me.honkling.commando.common.Commando
import me.honkling.commando.common.exception.ParseError
import me.honkling.commando.common.node.Node
import me.honkling.commando.common.parser.handle.FunctionHandle
import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.createType
import kotlin.reflect.jvm.kotlinFunction

data class ClassParseResult(
    val interactionType: InteractionType<*,  *>,
    val node: Node<*>
)

fun parseClass(commando: Commando, clazz: KClass<*>): Result<List<ClassParseResult>> {
    commando.logger.finest("Testing class ${clazz.qualifiedName}")
    val parseResults = mutableListOf<ClassParseResult>()

    for (interaction in commando.interactionRegistry.interactionTypes) {
        if (interaction.testParent(clazz).isFailure) {
            commando.logger.finest("Test parent on class failed")
            continue
        }

        val node = interaction.createRootNode(clazz)
        commando.logger.finest("Created node $node")

        for ((java, kotlin) in clazz.java.declaredMethods.mapNotNull {
            it to (it.kotlinFunction ?: return@mapNotNull null)
        }) {
            commando.logger.finest("Testing function ${kotlin.name} ($java) ($kotlin)")
            commando.logger.finest("${kotlin.returnType != Unit::class} (${kotlin.returnType} != ${Unit::class}) - ${!Modifier.isStatic(java.modifiers)}")
            commando.logger.finest("${java.declaringClass.name}")

            if (!Modifier.isStatic(java.modifiers))
                continue

            @Suppress("UNCHECKED_CAST")
            kotlin as KFunction<Unit>
            val handle = FunctionHandle(interaction, kotlin)
            val testResult = interaction.testFunction(clazz, handle)

            if (testResult.isFailure) {
                val exception = testResult.exceptionOrNull()!!
                commando.logger.fine("Skipping function '${handle.name}' of class '${clazz.qualifiedName}': ${exception.message}")
                continue
            }

            commando.logger.finest("Parsing function")

            // Reflection for now because Kotlin is being really weird
            // about generics and I don't feel like figuring it out !!

            (interaction::parse as KFunction<Unit>).call(node, clazz, handle)
        }

        for (child in clazz.nestedClasses) {
            val parsedChild = parseClass(commando, child)

            if (parsedChild.isSuccess) {
                val parseResult = parsedChild.getOrThrow()
                    .find { it.interactionType == interaction }

                if (parseResult != null) {
                    val childNode = parseResult.node
                    childNode.parent = node
                    node.children += childNode
                }
            }
        }

        parseResults += ClassParseResult(interaction, node)
    }

    if (parseResults.isEmpty())
        return Result.failure(ParseError.NoCandidates("No interaction types matched."))

    return Result.success(parseResults)
}