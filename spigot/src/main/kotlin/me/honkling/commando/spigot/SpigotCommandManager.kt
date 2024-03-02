package me.honkling.commando.spigot

import me.honkling.commando.common.CommandManager
import me.honkling.commando.common.tabComplete
import me.honkling.commando.common.tree.CommandNode
import me.honkling.commando.spigot.impl.Plugin
import me.honkling.commando.spigot.impl.SenderProvider
import me.honkling.commando.spigot.types.OfflinePlayerType
import me.honkling.commando.spigot.types.PlayerType
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.command.PluginCommand
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class SpigotCommandManager(plugin: JavaPlugin) : CommandManager(Plugin(plugin)) {
    init {
        types[Player::class.java] = PlayerType
        types[OfflinePlayer::class.java] = OfflinePlayerType
    }

    override fun isValidSender(clazz: Class<*>): Boolean {
        return clazz in listOf(
            Player::class.java,
            ConsoleCommandSender::class.java,
            CommandSender::class.java
        )
    }

    override fun registerCommand(node: CommandNode) {
        val commandMap = Bukkit.getCommandMap()

        for ((_, node) in commands) {
            val command = createPluginCommand(node)
            command.setExecutor(::onCommand)

            command.setTabCompleter { sender, command, _, args ->
                val node = commands[command.name] ?: return@setTabCompleter emptyList()
                return@setTabCompleter tabComplete(this, SenderProvider(sender), node, args)
            }

            commandMap.register((plugin as Plugin).plugin.name, command)
        }
    }

    private fun onCommand(sender: CommandSender, bukkitCommand: Command, label: String, args: Array<String>): Boolean {
        val provider = SenderProvider(sender)
        val command = commands[bukkitCommand.name.lowercase()] ?: return false
        val (subcommand, parameters) = getCommand(provider, command, args.toList()) ?: return false

        subcommand.method.invoke(null, sender, *parameters.toTypedArray())

        return true
    }

    private fun createPluginCommand(node: CommandNode): PluginCommand {
        val mm = MiniMessage.miniMessage()
        val constructor = PluginCommand::class.java.declaredConstructors[0]
        constructor.isAccessible = true
        val command = constructor.newInstance(node.name, (plugin as Plugin).plugin) as PluginCommand

        command.description = node.description
        command.usage = LegacyComponentSerializer.legacySection().serialize(mm.deserialize(node.usage.replace("{0}", node.name)))
        command.aliases = node.aliases
        command.permission = node.permission.replace("{0}", node.name)
        command.permissionMessage(mm.deserialize(node.permissionMessage))

        return command
    }
}