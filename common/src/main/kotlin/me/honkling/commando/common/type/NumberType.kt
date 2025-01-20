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
        val isNegative = input.firstOrNull() == '-'
        val factor = if (isNegative) -1 else 1

        var multiplier = 10.0
        var doubleValue = 0.0
        var value = 0L
        var index = if (isNegative) 1 else 0

        if (input.getOrNull(index) == 'i') {
            // Check for infinity
            val builder = StringBuilder()

            while (index < input.length && input[index] != ' ')
                builder.append(input[index++])

            val identifier = builder.toString()

            if (identifier != "inf")
                return Result.failure(IllegalArgumentException(
                    "Expected a number, inf, or -inf. Received '$identifier'"))

            return Result.success(when (typeClass) {
                Float::class, java.lang.Float::class,
                Double::class, java.lang.Double::class -> Double.POSITIVE_INFINITY * factor
                else -> return Result.failure(IllegalArgumentException("Infinity isn't supported here."))
            } as T to input.substring(index))
        }

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
            Byte::class, java.lang.Byte::class -> value.toByte() * factor
            Short::class, java.lang.Short::class -> value.toShort() * factor
            Int::class, java.lang.Integer::class -> value.toInt() * factor
            Long::class, java.lang.Long::class -> value * factor
            Float::class, java.lang.Float::class -> doubleValue.toFloat() * factor
            Double::class, java.lang.Double::class -> doubleValue * factor
            else -> return Result.failure(IllegalStateException("Unknown number type '${typeClass.qualifiedName}'"))
        } as T to newInput)
    }

    override fun suggest(user: User<*>, node: Node<*>, input: String): List<String> {
        val first = input(input, 1)

        return when (typeClass) {
            Float::class, java.lang.Float::class,
            Double::class, java.lang.Double::class -> listOf("inf", "-inf")
            else -> emptyList()
        }.filter { first in it }
    }
}