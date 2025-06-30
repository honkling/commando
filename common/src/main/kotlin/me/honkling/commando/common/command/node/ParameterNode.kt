package me.honkling.commando.common.command.node

import me.honkling.commando.common.Commando
import me.honkling.commando.common.command.ParameterInfo
import me.honkling.commando.common.node.AutoCompletable
import me.honkling.commando.common.node.Node
import me.honkling.commando.common.node.Parsable
import me.honkling.commando.common.platform.User
import java.lang.reflect.Array

class ParameterNode<Anno : Annotation>(
    commando: Commando,
    parent: SubCommandNode<Anno>,
    name: String,
    context: ParameterInfo
) : Node<ParameterInfo>(commando, parent, name, context), Parsable<Pair<Any?, String>>, AutoCompletable {
    override fun parse(user: User<*>, input: String): Result<Pair<Any?, String>> {
        return parse(user, input, false)
    }

    fun parse(user: User<*>, input: String, autoCompleting: Boolean): Result<Pair<Any?, String>> {
        val (klass, type, isRequired, isVararg) = context

        if (!isRequired && input.isEmpty())
            return Result.success(null to input)

        if (isVararg) {
            val parentChildren = parent!!.children
            val myIndex = parentChildren.indexOf(this)
            val postParameters = parentChildren.subList(myIndex + 1, parentChildren.size)
            val parsed = mutableListOf<Any?>()
            var input = input

            while (input.isNotEmpty()) {
                var checkInput = input
                var parseAnother = false
                for ((index, parameter) in postParameters.withIndex()) {
                    parameter as ParameterNode<Anno>
                    val result = parameter.parse(user, checkInput, autoCompleting)

                    if (result.isFailure) {
                        parseAnother = true
                        break
                    }

                    val (_, remainingInput) = result.getOrThrow()

                    checkInput = remainingInput
                    if (index + 1 >= postParameters.size && checkInput.isNotEmpty())
                        parseAnother = true
                }

                commando.logger.finest("Should parse an arg: $parseAnother")

                if (!parseAnother)
                    break

                commando.logger.finest("Parsing vararg. Input: '$input'")
                val result = type.parse(user, this, input, autoCompleting)
                commando.logger.finest(result.toString())

                if (result.isFailure)
                    return result

                val (value, remainingInput) = result.getOrThrow()
                input = remainingInput.trimStart()
                parsed += value
            }

            val array = Array.newInstance(klass.java, parsed.size)

            for ((index, value) in parsed.withIndex())
                Array.set(array, index, value)

            return Result.success(array to input)
        }

        return type.parse(user, this, input, autoCompleting)
    }

    override fun autoComplete(user: User<*>, input: String): List<String> {
        val (_, type) = context
        commando.logger.finest("Auto completing parameter with input '$input'")
        val completions = type.suggest(user, this, input).toMutableList()
        val subCommand = parent as SubCommandNode<*>
        val completors = parent!!.parent!!.children
            .filterIsInstance<CompletionNode<Anno>>()
            .filter { it.handle.name.substringBefore("\$complete") == subCommand.name }

        for (completor in completors)
            completions += completor.autoComplete(user, this, input)

        return completions
    }

    override fun toString(): String {
        return "Parameter${super.toString()}"
    }
}