package me.honkling.commando.common.command

import me.honkling.commando.common.type.Type

data class ParameterInfo(
    val type: Type<*>,
    val isRequired: Boolean
)