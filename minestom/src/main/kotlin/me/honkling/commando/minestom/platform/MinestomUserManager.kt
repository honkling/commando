package me.honkling.commando.minestom.platform

import me.honkling.commando.common.platform.User
import me.honkling.commando.common.platform.UserManager
import net.minestom.server.command.CommandSender

class MinestomUserManager : UserManager<CommandSender>() {
    override fun createUser(accessor: CommandSender): User<CommandSender> {
        return MinestomUser(accessor)
    }
}