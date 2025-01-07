package me.honkling.commando.minestom.type

import me.honkling.commando.common.node.Node
import me.honkling.commando.common.platform.User
import me.honkling.commando.common.type.Type
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import java.util.UUID

private val connections = MinecraftServer.getConnectionManager()

class PlayerType : Type<Player>() {
    override fun parse(
        user: User<*>,
        node: Node<*>,
        input: String,
        autoCompleting: Boolean
    ): Result<Pair<Player, String>> {
        val first = input(input, 1)
        val rest = input.substringAfter(' ', "")
        lateinit var player: Player

        if (autoCompleting && ' ' !in input)
            return Result.failure(IllegalArgumentException("Override"))

        if (first.isEmpty())
            return Result.failure(IllegalArgumentException("Expected a player name or UUID, found nothing"))

        try {
            val uuid = UUID.fromString(first)
            player = connections.getOnlinePlayerByUuid(uuid)
                ?: return Result.failure(IllegalArgumentException("Failed to find the player with the UUID '$first'"))
        } catch (ignored: IllegalArgumentException) {
            player = connections.getOnlinePlayerByUsername(first)
                ?: return Result.failure(IllegalArgumentException("Failed to find the player named '$first'"))
        }

        return Result.success(player to rest)
    }

    override fun suggest(user: User<*>, node: Node<*>, input: String): List<String> {
        val first = input(input, 1)
        return connections.onlinePlayers
            .map(Player::getUsername)
            .filter { first in it }
    }
}