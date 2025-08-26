package me.honkling.commando.brigadier

import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import me.honkling.commando.common.Commando
import me.honkling.commando.common.command.node.CommandNode
import me.honkling.commando.common.command.node.CompletionNode
import me.honkling.commando.common.command.node.ParameterNode
import me.honkling.commando.common.command.node.SubCommandNode
import me.honkling.commando.common.node.Node
import me.honkling.commando.common.type.Type
import kotlin.collections.map
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.instanceParameter

fun <S : Any> convertNodeTree(commando: Commando, node: Node<*>, builder: ArgumentBuilder<S, *>?): ArgumentBuilder<S, *> {
    return when (node) {
        is CommandNode<*>, is SubCommandNode<*> -> {
            val newRoot = LiteralArgumentBuilder.literal<S>(node.name)

            node.children.map { convertNodeTree<S>(commando, it, newRoot) }
                .forEach { newRoot.then(it) }

            newRoot
        }
        is ParameterNode<*> -> {
            val type = TypeMapper(commando, node, node.context.type)
            val func = RequiredArgumentBuilder::class.declaredFunctions.find {
                it.instanceParameter == null && it.name == "argument"
            }!!

            val newNode = func.call(node.name, type) as ArgumentBuilder<S, *>
            builder!!.then(newNode)

            // todo: optional args
            //if (!node.context.isRequired)
            //    builder.

            builder
        }
        is CompletionNode<*> -> {
            // todo: completion nodes
            builder!!
        }
        // unsupported node
        else -> builder!!
    }
}