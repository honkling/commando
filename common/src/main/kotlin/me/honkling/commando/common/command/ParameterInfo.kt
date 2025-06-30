package me.honkling.commando.common.command

import me.honkling.commando.common.type.Type
import kotlin.reflect.KClass

data class ParameterInfo(
    val klass: KClass<*>,
    val type: Type<*>,
    val isRequired: Boolean,
    val isVararg: Boolean
)