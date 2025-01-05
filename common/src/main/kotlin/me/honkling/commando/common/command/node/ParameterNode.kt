package me.honkling.commando.common.command.node

import me.honkling.commando.common.command.ParameterInfo
import me.honkling.commando.common.node.AutoCompletable
import me.honkling.commando.common.node.Node
import me.honkling.commando.common.node.Parsable
import me.honkling.commando.common.platform.User

class ParameterNode<Anno : Annotation>(
    parent: SubCommandNode<Anno>,
    name: String,
    context: ParameterInfo
) : Node<ParameterInfo>(parent, name, context), Parsable<Pair<Any?, String>>, AutoCompletable {
    override fun parse(user: User<*>, input: String): Result<Pair<Any?, String>> {
        return parse(user, input, false)
    }

    fun parse(user: User<*>, input: String, autoCompleting: Boolean): Result<Pair<Any?, String>> {
        val (type, isRequired) = context

        if (!isRequired && input.isEmpty())
            return Result.success(null to input)

        return type.parse(user, this, input, autoCompleting)
    }

    override fun autoComplete(user: User<*>, input: String): List<String> {
        val (type) = context
        println("Auto completing parameter with input '$input'")
        val completions = type.suggest(user, this, input)
        println(completions)

        return completions
    }

    override fun toString(): String {
        return "Parameter${super.toString()}"
    }
}