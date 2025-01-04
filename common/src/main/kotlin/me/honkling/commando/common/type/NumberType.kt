package me.honkling.commando.common.type

import me.honkling.commando.common.node.Node
import me.honkling.commando.common.platform.User
import java.math.BigInteger
import kotlin.reflect.KClass
import kotlin.time.times

class NumberType<T : Number>(
    private val typeClass: KClass<T>
) : Type<T>() {
    private val isFloatingPoint = typeClass in listOf<KClass<out Number>>(
        Float::class,
        java.lang.Float::class,
        Double::class,
        java.lang.Double::class
    )

    override fun parse(user: User<*>, node: Node<*>, input: String, autoCompleting: Boolean): Result<Pair<T, String>> {
        var multiplier = 10.0
        var doubleValue = 0.0
        var value = 0L
        var index = 0

        while (index < input.length && (input[index].isDigit() || (input[index] == '.' && multiplier == 10.0 && isFloatingPoint))) {
            val char = input[index++]

            if (char == '.') {
                multiplier = 0.1
                continue
            }

            val digitValue = char.digitToInt()

            if (multiplier == 10.0) {
                value *= 10
                doubleValue *= 10
                value += digitValue
                doubleValue += digitValue
            } else {
                doubleValue += digitValue * multiplier
                multiplier /= 10
            }
        }

        val newInput = input.substring(index)
        return Result.success(when (typeClass) {
            Byte::class, java.lang.Byte::class -> value.toByte()
            Short::class, java.lang.Short::class -> value.toShort()
            Int::class, java.lang.Integer::class -> value.toInt()
            Long::class, java.lang.Long::class -> value
            Float::class, java.lang.Float::class -> doubleValue.toFloat()
            Double::class, java.lang.Double::class -> doubleValue
            else -> return Result.failure(IllegalStateException("Unknown number type '${typeClass.qualifiedName}'"))
        } as T to newInput)
    }

    override fun suggest(user: User<*>, node: Node<*>, input: String): List<String> {
        return emptyList()
    }
}