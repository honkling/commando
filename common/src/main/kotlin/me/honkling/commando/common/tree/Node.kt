
package me.honkling.commando.common.tree

abstract class Node(val parent: Node?, val name: String) {
	val children = mutableListOf<Node>()

	protected fun stringify(string: String): String {
		return "\"${string.replace("\\", "\\\\").replace("\"", "\\\"")}\""
	}

	abstract override fun toString(): String
}