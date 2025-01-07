package me.honkling.commando.common.command.node

import me.honkling.commando.common.exception.ParseError
import me.honkling.commando.common.node.AutoCompletable
import me.honkling.commando.common.node.Node
import me.honkling.commando.common.node.Parsable
import me.honkling.commando.common.parser.handle.FunctionHandle
import me.honkling.commando.common.platform.User
import kotlin.reflect.KFunction

class SubCommandNode<Anno : Annotation>(
    parent: CommandNode<Anno>,
    name: String,
    val handle: FunctionHandle
) : Node<Nothing?>(parent, name, null), Parsable<List<Any?>>, AutoCompletable {
    override fun parse(user: User<*>, input: String): Result<List<Any?>> {
        val parent = parent!!

        if (!input.startsWith(name) && parent.name != name)
            return Result.failure(ParseError.NotApplicable("Not a valid sub command"))

        if (parent.name == name) {
            val first = input.split(" ")[0]

            if (parent.children.any { it.name == first })
                return Result.failure(ParseError.NotApplicable("Not a valid sub command"))
        }

        val parameters = mutableListOf<Any?>()
        @Suppress("NAME_SHADOWING")
        var input =
            if (parent.name != name) input.substringAfter(' ', "")
            else input

        for (parameter in children) {
            println("Parsing with input '$input'")
            parameter as ParameterNode<*>
            val parseResult = parameter.parse(user, input)

            if (parseResult.isFailure)
                return Result.failure(parseResult.exceptionOrNull()!!)

            // It should *never* throw, but if it does, it's better to log
            // that exception than to throw a generic NullPointerException.
            val (value, newInput) = parseResult.getOrThrow()
            parameters += value
            input = newInput.trim()
        }

        return Result.success(parameters)
    }

    override fun autoComplete(user: User<*>, input: String): List<String> {
        @Suppress("NAME_SHADOWING")
        var input = input

        for (parameter in children as List<ParameterNode<Anno>>) {
            println("Now trying parameter ${parameter.context} with input '${input}'")
            val parseResult = parameter.parse(user, input, true)
            println(parseResult)

            if (parseResult.isSuccess) {
                val newInput = parseResult.getOrThrow().second

                if (' ' !in input)
                    return parameter.autoComplete(user, input)

                input = newInput.trimStart()
                continue
            }

            return parameter.autoComplete(user, input)
        }

        return emptyList()
    }

    override fun toString(): String {
        return "SubCommand${super.toString()}"
    }
}