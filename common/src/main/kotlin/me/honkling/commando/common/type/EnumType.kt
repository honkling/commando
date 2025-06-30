package me.honkling.commando.common.type

import me.honkling.commando.common.node.Node
import me.honkling.commando.common.platform.User
import kotlin.enums.EnumEntries
import kotlin.reflect.KClass

class EnumType<T : Enum<T>>(
    enumClass: KClass<T>
) : Type<T>() {
    private val values: List<T>

    init {
        val java = enumClass.java
        val entries = java.declaredMethods.find { it.name == "getEntries" }
            ?: java.declaredMethods.find { it.name == "values" }!!

        entries.isAccessible = true
        val value = entries.invoke(null)

        @Suppress("UNCHECKED_CAST")
        values = when (value) {
            is Array<*> -> value.toList()
            is EnumEntries<*> -> value.toList()
            else -> throw IllegalStateException("Unexpected type ${value::class.java.name} for enum entries")
        } as List<T>
    }

    override fun parse(
        user: User<*>,
        node: Node<*>,
        input: String,
        autoCompleting: Boolean
    ): Result<Pair<T, String>> {
        val first = input(input, 1)
        val value = values.find { it.name == first }
            ?: values.firstOrNull { it.name.equals(first, true) }
            ?: return Result.failure(IllegalArgumentException("Expected one of ${values.joinToString("/") { it.name }}, but found '$first' instead."))

        node.commando.logger.finest(value.toString())
        return Result.success(value to input(input, 1, true))
    }

    override fun suggest(user: User<*>, node: Node<*>, input: String): List<String> {
        return values
            .map(Enum<T>::name)
            .filter { it.contains(input, true) }
    }
}