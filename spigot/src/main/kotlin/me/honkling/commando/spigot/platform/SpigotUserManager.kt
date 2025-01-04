package me.honkling.commando.spigot.platform

import me.honkling.commando.common.platform.User
import me.honkling.commando.common.platform.UserManager
import org.bukkit.command.CommandSender

class SpigotUserManager : UserManager<CommandSender>() {
    override fun createUser(accessor: CommandSender): User<CommandSender> {
        return SpigotUser(accessor)
    }
}