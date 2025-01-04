package me.honkling.commando.common.parser

data class ExecutionResult<Interaction>(
    val interaction: Interaction,
    val parameters: List<Any>
)
