package me.honkling.commando.minestom

import me.honkling.commando.common.Commando
import me.honkling.commando.minestom.command.CommandInteraction
import me.honkling.commando.minestom.event.EventInteraction
import me.honkling.commando.minestom.platform.MinestomUserManager
import me.honkling.commando.minestom.type.PlayerType
import net.minestom.server.command.CommandSender
import net.minestom.server.entity.Player
import kotlin.reflect.KClass

class MinestomCommando(
    instanceClass: KClass<*>,
    val canAccessBlock: (sender: CommandSender, command: String?, permission: String) -> Boolean = { _, _, _ -> true }
) : Commando(instanceClass) {
    override val userManager = MinestomUserManager()

    init {
        interactionRegistry.register(CommandInteraction(this))
        interactionRegistry.register(EventInteraction(this))
        typeRegistry.register(PlayerType(), Player::class)
    }
}