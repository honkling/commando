package me.honkling.commando.spigot.type

import me.honkling.commando.common.node.Node
import me.honkling.commando.common.platform.User
import me.honkling.commando.common.type.Type
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.UUID

class OfflinePlayerType : Type<OfflinePlayer>() {
    override fun parse(
        user: User<*>,
        node: Node<*>,
        input: String,
        autoCompleting: Boolean
    ): Result<Pair<OfflinePlayer, String>> {
        val first = input(input, 1)
        val rest = input(input, 1, true)
        lateinit var player: OfflinePlayer

        if (autoCompleting && ' ' !in input)
            return Result.failure(IllegalArgumentException("Override"))

        if (first.isEmpty())
            return Result.failure(IllegalArgumentException("Expected a player name or UUID, found nothing"))

        try {
            val uuid = UUID.fromString(first)
            player = Bukkit.getOfflinePlayer(uuid)
        } catch (ignored: IllegalArgumentException) {
            player = Bukkit.getOfflinePlayer(first)
        }

        return Result.success(player to rest)
    }

    override fun suggest(user: User<*>, node: Node<*>, input: String): List<String> {
        val first = input(input, 1)
        return Bukkit.getOnlinePlayers()
            .map(Player::getName)
            .filter { first in it }
    }
}