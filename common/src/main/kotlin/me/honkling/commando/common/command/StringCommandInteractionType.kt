package me.honkling.commando.common.command

import me.honkling.commando.common.Commando
import me.honkling.commando.common.command.node.CommandNode
import me.honkling.commando.common.command.node.CompletionNode
import me.honkling.commando.common.command.node.ParameterNode
import me.honkling.commando.common.command.node.SubCommandNode
import me.honkling.commando.common.exception.ExecutionError
import me.honkling.commando.common.node.Node
import me.honkling.commando.common.parser.ContextProvider
import me.honkling.commando.common.parser.InteractionType
import me.honkling.commando.common.parser.getContextProviderType
import me.honkling.commando.common.parser.handle.FunctionHandle
import me.honkling.commando.common.platform.User
import me.honkling.commando.common.type.EnumType
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaMethod

abstract class StringCommandInteractionType<Sender : Any, Event : Any, Anno : Annotation>(
    protected open val commando: Commando,
    private val senderClass: KClass<Sender>,
    private val annotationClass: KClass<Anno>
) : InteractionType<Event, Anno>() {
    override fun testParent(parent: KClass<*>): Result<Nothing?> {
        if (parent.java.annotations.none { it.annotationClass == annotationClass })
            return Result.failure(IllegalArgumentException("Command doesn't have an annotation"))

        return Result.success(null)
    }

    override fun testFunction(parent: KClass<*>, handle: FunctionHandle): Result<Nothing?> {
        val parameters = handle.parameters.toMutableList()

        val isSenderValid = parameters.removeFirstOrNull()?.first
            ?.let { senderClass.java.isAssignableFrom(it.type) } == true

        if (handle.name.endsWith("\$complete")) {
            if (!isSenderValid)
                return Result.failure(IllegalArgumentException("First parameter of completion function must be a command sender"))

            if (parameters.removeFirstOrNull()?.first?.type != ParameterNode::class.java)
                return Result.failure(IllegalArgumentException("Second parameter of completion function must be a ParameterNode"))

            if (parameters.removeFirstOrNull()?.first?.type != String::class.java)
                return Result.failure(IllegalArgumentException("Third parameter of completion function must be a String"))

            if (parameters.isNotEmpty())
                return Result.failure(IllegalArgumentException("Completion function must not have more than three parameters"))

            return Result.success(null)
        }

        val invalidParameterTypes = parameters
            .filter {
                val type = it.first.type.kotlin
                type !in commando.typeRegistry && !type.isSubclassOf(Enum::class)
            }

        val earliestOptional = parameters.indexOfFirst { it.second.isOptional }

        if (earliestOptional != -1) {
            val badParameters = parameters.slice(earliestOptional..<parameters.size)
                .filter { !it.second.isOptional }

            if (badParameters.isNotEmpty()) {
                val names = "\"${invalidParameterTypes.joinToString("\", \"") { it.second.name ?: it.first.name }}\""
                return Result.failure(IllegalArgumentException("Parameters $names are required, but take place after an optional parameter. This is not supported."))
            }
        }

        if (!isSenderValid)
            return Result.failure(IllegalArgumentException("Command doesn't have a command sender"))

        if (invalidParameterTypes.isNotEmpty()) {
            val names = "\"${invalidParameterTypes.joinToString("\", \"") { it.second.name ?: it.first.name }}\""
            return Result.failure(IllegalArgumentException("Parameters $names have unrecognized types"))
        }

        return Result.success(null)
    }

    override fun parse(root: Node<Anno>, parent: KClass<*>, handle: FunctionHandle) {
        commando.logger.finest("Parsing function '${handle.name}'")
        root as CommandNode<Anno>

        if (handle.name.endsWith("\$complete")) {
            root.children += CompletionNode(commando, root, handle.name, handle)
            return
        }

        val node = SubCommandNode(commando, root, handle.name, handle)

        for ((java, kotlin) in handle.parameters.slice(1..<handle.parameters.size)) {
            commando.logger.finest("Java parameter: ${java.name} ${java.type}")
            commando.logger.finest("Kotlin parameter: ${kotlin.name} ${kotlin.type}")
            @Suppress("UNCHECKED_CAST")
            val type = commando.typeRegistry[java.type.kotlin]
                ?: EnumType(java.type.kotlin as KClass<out Enum<*>>)

            node.children += ParameterNode(
                commando,
                node,
                kotlin.name ?: java.name,
                ParameterInfo(type, !kotlin.isOptional)
            )
        }

        root.children += node
    }

    fun execute(root: Node<Anno>, user: User<*>, input: String, context: Event): Result<Nothing?> {
        root as CommandNode<Anno>
        val parseResult = root.parse(user, input)

        if (parseResult.isFailure) {
            val exception = parseResult.exceptionOrNull()!!
            return Result.failure(ExecutionError.BadInput("${exception.message}"))
        }

        val (subCommand, parameters) = parseResult.getOrThrow()
        val callParameters = parameters.toMutableList()
        val function = subCommand.handle.reflector
        val contextType = function.getContextProviderType()
        val declaringClass = function.javaMethod!!.declaringClass
        val instanceField = declaringClass.declaredFields.find { it.name == "INSTANCE" }
        instanceField?.isAccessible = true
        val objectInstance = instanceField?.get(null)
        val instanceParameter = function.instanceParameter

        if (!subCommand.handle.parameters[0].first.type.isAssignableFrom(user.accessor::class.java))
            return Result.failure(ExecutionError.BadInput("You cannot run this command."))

        callParameters.add(0, user.accessor)

        if (contextType != null) {
            val creator = contexts[contextType]
                ?: return Result.failure(ExecutionError("No context creator is registered for context provider '${contextType.qualifiedName}'"))

            val contextProvider = creator.block(root, context)
            callParameters.add(0, contextProvider)
        }

        commando.logger.finest(callParameters.toString())
        commando.logger.finest(function.parameters.map { it.type }.toString())
        val parameterMap = mutableMapOf(*callParameters.mapIndexedNotNull { index, it ->
            commando.logger.finest("$index to $it")
            val rightIndex = index + if (instanceParameter != null || objectInstance != null) 1 else 0
            val parameter = function.parameters[rightIndex]

            if (it == null)
                null
            else parameter to it
        }.toTypedArray())
        commando.logger.finest(parameterMap.map { it.key.name to it.value }.toString())

        if (function.instanceParameter != null) {
            if (objectInstance != null) {
                val parameter = function.parameters.first()
                parameterMap[parameter] = objectInstance
            } else {
                // This *must* have been defined in a companion object. Let's get the instance and pass it along.
                val field = declaringClass.declaringClass.getDeclaredField("Companion")
                field.isAccessible = true
                parameterMap[function.instanceParameter!!] = field[null]
            }
        }

        function.isAccessible = true
        function.callBy(parameterMap)
        return Result.success(null)
    }
}