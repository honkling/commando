package me.honkling.commando.brigadier

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import kotlinx.coroutines.launch
import me.honkling.commando.common.Commando
import me.honkling.commando.common.command.node.ParameterNode
import me.honkling.commando.common.type.Type
import java.util.concurrent.CompletableFuture

class TypeMapper<T>(
    val commando: Commando,
    val node: ParameterNode<*>,
    val type: Type<T>
) : ArgumentType<T> {

    override fun parse(reader: StringReader): T? {
        throw IllegalStateException("Commando doesn't support parsing types that don't provide a source.")
    }

    override fun <S : Any> parse(reader: StringReader, source: S): T? {
        val input = reader.remaining
        val user = commando.userManager::getUser.call(source)
        val result = type.parse(user, node, input, false)

        if (result.isFailure)
            return null

        val (value, newInput) = result.getOrThrow()

        if (!input.endsWith(newInput))
            throw IllegalStateException("Original input of mapped type must end with new input")

        reader.cursor += input.lastIndexOf(newInput)
        return value
    }

    override fun <S : Any?> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val future = CompletableFuture<Suggestions>()

        scope.launch {
            val input = context.input
            val user = commando.userManager::getUser.call(context.source)
            val suggestions = type.suggest(user, node, input)

            for (suggestion in suggestions)
                builder.suggest(suggestion)

            future.complete(builder.build())
        }

        return future
    }
}