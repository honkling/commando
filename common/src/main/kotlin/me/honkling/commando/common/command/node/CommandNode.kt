package me.honkling.commando.common.command.node

import me.honkling.commando.common.exception.ParseError
import me.honkling.commando.common.node.AutoCompletable
import me.honkling.commando.common.node.Node
import me.honkling.commando.common.node.Parsable
import me.honkling.commando.common.platform.User

class CommandNode<Anno : Annotation>(
    parent: CommandNode<Anno>?,
    name: String,
    context: Anno
) : Node<Anno>(parent, name, context), Parsable<Pair<SubCommandNode<Anno>, List<Any?>>>, AutoCompletable {
    override fun parse(user: User<*>, input: String): Result<Pair<SubCommandNode<Anno>, List<Any?>>> {
        for (child in children) {
            if (child is SubCommandNode<*>) {
                child as SubCommandNode<Anno>
                val parseResult = child.parse(user, input)

                if (parseResult.isSuccess) {
                    val parameters = parseResult.getOrThrow()
                    return Result.success(child to parameters)
                }

                val exception = parseResult.exceptionOrNull()!!
                if (exception is ParseError.NotApplicable)
                    continue

                return Result.failure(exception)
            } else if (child is CommandNode<*>) {
                child as CommandNode<Anno>

                println("'$input' ('${input.split(" ").firstOrNull()}' vs '${child.name}')")

                if (input.split(" ").firstOrNull() != child.name)
                    continue

                return child.parse(user, input.substringAfter(' ', ""))
            }
        }

        return Result.failure(ParseError.NoCandidates("Bad input; Failed to find a matching command."))
    }

    override fun autoComplete(user: User<*>, input: String): List<String> {
        val defaultNode = children.find { it.name == name }

        if (' ' !in input) {
            val completions =
                if (defaultNode is AutoCompletable)
                    defaultNode.autoComplete(user, input).toMutableList()
                else mutableListOf()

            completions += children
                .mapNotNull { if (it != defaultNode) it.name else null }

            return completions
                .filter { input in it }
        }

        val first = input.split(" ")[0]
        val subCommand = children.find { it.name == first }

        if (subCommand != null) {
            subCommand as AutoCompletable
            return subCommand.autoComplete(user, input.substringAfter(' '))
        }

        // The user provided a bad sub command or something else
        return emptyList()
    }

    override fun toString(): String {
        return "Command${super.toString()}"
    }
}