package me.honkling.commando.common.tree

data class Parameter(
        val name: String,
        val type: Class<*>,
        val required: Boolean,
        val greedy: Boolean
)
