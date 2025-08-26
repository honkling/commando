package me.honkling.commando.common.command.node

import me.honkling.commando.common.Commando
import me.honkling.commando.common.exception.ParseError
import me.honkling.commando.common.node.AutoCompletable
import me.honkling.commando.common.node.Node
import me.honkling.commando.common.node.Parsable
import me.honkling.commando.common.platform.User

class CommandNode<Anno : Annotation>(
    commando: Commando,
    parent: CommandNode<Anno>?,
    name: String,
    context: Anno
) : Node<Anno>(commando, parent, name, context), Parsable<Pair<SubCommandNode<Anno>, List<Any?>>>, AutoCompletable {
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

                commando.logger.finest("'$input' ('${input.split(" ").firstOrNull()}' vs '${child.name}')")

                if (input.split(" ").firstOrNull() != child.name)
                    continue

                return child.parse(user, input.substringAfter(' ', ""))
            }
        }

        return Result.failure(ParseError.NoCandidates("Bad input; Failed to find a matching command."))
    }

    override fun autoComplete(user: User<*>, input: String): List<String> {
        commando.logger.finest("Request: '$input'")
        @Suppress("UNCHECKED_CAST")
        val defaultNodes = children.filter { it is SubCommandNode<*> && it.name == name } as List<SubCommandNode<Anno>>

        if (' ' !in input) {
            val completions = defaultNodes.flatMap {
                (it as? AutoCompletable)?.autoComplete(user, input)
                    ?: emptyList()
            }.toMutableList()

            completions += children.mapNotNull { if (it !in defaultNodes && it is SubCommandNode<*>) it.name else null }
                .filter { it.contains(input, true) }

            return completions
        }

        val first = input.split(" ")[0]
        val subCommand = children.find { it is SubCommandNode<*> && it.name == first }

        if (subCommand != null) {
            subCommand as AutoCompletable
            commando.logger.finest("Found subcommand $subCommand")
            commando.logger.finest("New input: '${input.substringAfter(' ', "")}'")
            return subCommand.autoComplete(user, input.substringAfter(' ', ""))
        }

        // The user provided a bad sub command or something else
        return defaultNodes.flatMap {
            (it as? AutoCompletable)?.autoComplete(user, input)
                ?: emptyList()
        }.toMutableList()
    }

    override fun toString(): String {
        return "Command${super.toString()}"
    }
}