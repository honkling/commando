package me.honkling.commando.common.tree

class CommandNode(
        parent: Node?,
        name: String,
        val aliases: List<String>,
        val description: String,
        val usage: String,
        val permission: String,
        val permissionMessage: String
) : Node(parent, name) {
    override fun toString(): String {
        return "CommandNode(name=$name)"
    }
}