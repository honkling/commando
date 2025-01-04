package me.honkling.commando.spigot.command

import me.honkling.commando.common.Commando
import me.honkling.commando.common.command.StringCommandInteractionType
import me.honkling.commando.common.command.node.CommandNode
import me.honkling.commando.common.command.node.ParameterNode
import me.honkling.commando.common.exception.ExecutionError
import me.honkling.commando.common.node.Node
import me.honkling.commando.common.parser.ContextProvider
import me.honkling.commando.common.parser.getContextProviderType
import me.honkling.commando.spigot.SpigotCommando
import me.honkling.commando.spigot.context.LabelContext
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.PluginCommand
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaMethod

data class SpigotContext(
    val node: CommandNode<Command>,
    val sender: CommandSender,
    val label: String,
    val input: String
)

class SpigotCommandInteraction(
    override val commando: SpigotCommando
) : StringCommandInteractionType<CommandSender, SpigotContext, Command>(
    commando,
    CommandSender::class,
    Command::class
) {
    init {
        addContextCreator { _, context ->
            LabelContext(context.label)
        }
    }

    override fun createRootNode(parent: KClass<*>): Node<Command> {
        val command = parent.java.getAnnotation(Command::class.java)
        return CommandNode(null, command.name, command)
    }

    override fun execute(root: Node<Command>, context: SpigotContext): Result<Unit> {
        root as CommandNode<Command>
        val user = commando.userManager.getUser(context.sender)
        return execute(root, user, context.input, context)
    }

    override fun postParse(root: Node<Command>) {
        println(root)
        root as CommandNode<Command>
        val commandMap = Bukkit.getCommandMap()
        val commandInfo = root.context
        val command = createCommand(commandInfo)

        command.setExecutor { sender, _, label, args ->
            val input = args.joinToString(" ")
            val context = SpigotContext(root, sender, label, input)
            val executionResult = execute(root, context)

            if (executionResult.isFailure) {
                val exception = executionResult.exceptionOrNull()!!

                if (exception is ExecutionError.BadInput)
                    context.sender.sendMessage(
                        Component
                            .text("Invalid usage: ${exception.message}")
                            .color(NamedTextColor.RED)
                    )
                else {
                    context.sender.sendMessage(
                        Component
                            .text("An error occurred executing the command. If you're a server administrator, please check logs.")
                            .color(NamedTextColor.RED)
                    )

                    commando.logger.severe(
                        """
                        An error occurred executing the command '/$label $input':
                        ${exception.message}
                    """.trimIndent()
                    )
                }
            }

            true
        }

        command.setTabCompleter { sender, _, label, args ->
            val user = commando.userManager.getUser(sender)
            val input = args.joinToString(" ")
            root.autoComplete(user, input)
        }

        commandMap.register(commando.plugin.pluginMeta.name, command)
    }

    private fun createCommand(command: Command): PluginCommand {
        val constructor = PluginCommand::class.java.declaredConstructors[0]
        constructor.isAccessible = true
        val pluginCommand = constructor.newInstance(command.name, commando.plugin) as PluginCommand

        pluginCommand.description = command.description
        pluginCommand.usage = "Invalid usage."
        pluginCommand.aliases = command.aliases.toList()
        pluginCommand.permission = command.permission.ifEmpty { null }

        return pluginCommand
    }
}