package me.honkling.commando.minestom

import me.honkling.commando.common.Commando
import me.honkling.commando.minestom.command.CommandInteraction
import me.honkling.commando.minestom.platform.MinestomUserManager
import me.honkling.commando.minestom.type.PlayerType
import net.minestom.server.entity.Player

class MinestomCommando : Commando() {
    override val userManager = MinestomUserManager()

    init {
        interactionRegistry.register(CommandInteraction(this))
        typeRegistry.register(PlayerType(), Player::class)
    }
}