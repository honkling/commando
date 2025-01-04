package me.honkling.commando.common.type

import me.honkling.commando.common.node.Node
import me.honkling.commando.common.platform.User

class StringType : Type<String>() {
    override fun parse(user: User<*>, node: Node<*>, input: String, autoCompleting: Boolean): Result<Pair<String, String>> {
        if (input.startsWith("\"")) {
            val builder = StringBuilder()
            var index = 1

            while (index < input.length) {
                val char = input[index]

                when (char) {
                    '\\' -> {
                        index++

                        if (index >= input.length)
                            return Result.failure(IllegalArgumentException("Expected character after '\\', but found nothing"))

                        builder.append(input[index++])
                        continue
                    }
                    '"' -> break
                }

                builder.append(input[index++])
            }

            if (index >= input.length)
                return Result.failure(IllegalArgumentException("Expected closing '\"', but found nothing"))

            index++
            return Result.success(builder.toString() to input.substring(index))
        }

        val value =
            if (node.parent!!.children.last { it::class == node::class } == node) input
            else input(input, 1)

        if (value.isEmpty())
            return Result.failure(IllegalArgumentException("Expected some input, but found nothing"))

        return Result.success(value to input.substring(value.length))
    }

    override fun suggest(user: User<*>, node: Node<*>, input: String): List<String> {
        return emptyList()
    }
}