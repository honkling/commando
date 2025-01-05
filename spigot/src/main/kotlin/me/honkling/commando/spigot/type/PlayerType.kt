package me.honkling.commando.spigot.type

import me.honkling.commando.common.node.Node
import me.honkling.commando.common.platform.User
import me.honkling.commando.common.type.Type
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

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
            player = Bukkit.getPlayer(uuid)
                ?: return Result.failure(IllegalArgumentException("Failed to find the player with the UUID '$first'"))
        } catch (ignored: IllegalArgumentException) {
            player = Bukkit.getPlayer(first)
                ?: return Result.failure(IllegalArgumentException("Failed to find the player named '$first'"))
        }

        return Result.success(player to rest)
    }

    override fun suggest(user: User<*>, node: Node<*>, input: String): List<String> {
        return Bukkit.getOnlinePlayers()
            .map(Player::getName)
            .filter { input in it }
    }
}