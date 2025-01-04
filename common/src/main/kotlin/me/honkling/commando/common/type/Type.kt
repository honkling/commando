package me.honkling.commando.common.type

import me.honkling.commando.common.node.Node
import me.honkling.commando.common.platform.User

/**
 * Represents a type which can be parsed, given input.
 *
 */
abstract class Type<T> {
    /**
     * Parses the current input and returns a value if the input is valid.
     * @param autoCompleting If the input is being parsed to move forward with auto completing.
     * @return A pair of the parsed object, and the new input (trimmed automatically.)
     */
    abstract fun parse(user: User<*>, node: Node<*>, input: String, autoCompleting: Boolean): Result<Pair<T, String>>

    /**
     * Gives suggestions of valid inputs based on the current input.
     * This can be an empty list if the platform doesn't support suggestions.
     */
    abstract fun suggest(user: User<*>, node: Node<*>, input: String): List<String>

    fun input(input: String, first: Int): String
        = input.split(" ")
            .slice(0..<first)
            .joinToString(" ")
}