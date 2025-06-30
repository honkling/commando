package me.honkling.commando.common.node

import me.honkling.commando.common.Commando

open class Node<Context>(
    val commando: Commando,
    open var parent: Node<*>?,
    val name: String,
    val context: Context
) {
    val children = mutableListOf<Node<*>>()

    override fun toString(): String {
        return "Node(name=$name, context=$context, children=$children)"
    }
}