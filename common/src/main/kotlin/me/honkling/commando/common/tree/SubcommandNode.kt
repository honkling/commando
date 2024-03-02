package me.honkling.commando.common.tree

import java.lang.reflect.Method

class SubcommandNode(
		parent: Node,
		val method: Method,
		name: String,
		val parameters: List<Parameter>
) : Node(parent, name) {
	override fun toString(): String {
		return "SubcommandNode(name=$name, parameters=$parameters, method=$method)"
	}
}