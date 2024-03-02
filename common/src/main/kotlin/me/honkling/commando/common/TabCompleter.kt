package me.honkling.commando.common

import me.honkling.commando.common.generic.ICommandSender
import me.honkling.commando.common.tree.CommandNode
import me.honkling.commando.common.tree.Node
import me.honkling.commando.common.tree.SubcommandNode

fun tabComplete(manager: CommandManager, sender: ICommandSender<*>, node: CommandNode, args: Array<String>): List<String> {
    val last = args.last()

    // /example
    // /example one (int)
    // /example two

    // /example (one, two) (command, args(0) -> command)
    // /example o (command, args(1) -> command)
    // /example one 1 (subcommand, args(1) -> subcommand)
    // /example two (subcommand, args(0) -> subcommand)

    val (completionNode, count) = getNode(node, args.toList())

    if (completionNode is CommandNode)
        return completionNode.children.map { it.name }.filter { it != completionNode.name }

    val arguments = args.toMutableList()

    for (i in 0..<count)
        arguments.removeFirst()

    if (arguments.isEmpty())
        return emptyList()

    for (parameter in (completionNode as SubcommandNode).parameters) {
        val type = manager.types[parameter.type] ?: return emptyList()
        val input = arguments.joinToString(" ")

        if (arguments.isEmpty() || !type.validate(sender, input)) {
            val parent = completionNode.parent

            return listOf(
                *type.complete(sender, input).toTypedArray(),
                *(if (completionNode.name == parent?.name)
                      parent.children.map { it.name }.filter { it != parent.name }.toTypedArray()
                  else emptyArray())
            )
        }

        val (_, count) = type.parse(sender, input)

        for (i in 0..<count)
            arguments.removeFirst()
    }

    return emptyList()
}

private fun getNode(node: CommandNode, args: List<String>, count: Int = 0): Pair<Node, Int> {
    if (args.isEmpty())
        return node to count

    val childNode = node.children.find { it.name == args[0] || it.name == node.name } ?: return node to count

    if (childNode is CommandNode)
        return getNode(childNode, args.slice(1..<args.size), count + 1)

    return childNode to count + 1
}