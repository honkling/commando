package me.honkling.commando.minestom.command

import me.honkling.commando.common.command.StringCommandInteractionType
import me.honkling.commando.common.command.node.CommandNode
import me.honkling.commando.common.exception.ExecutionError
import me.honkling.commando.common.node.Node
import me.honkling.commando.common.parser.handle.FunctionHandle
import me.honkling.commando.minestom.MinestomCommando
import me.honkling.commando.minestom.context.AliasContext
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.SimpleCommand
import net.minestom.server.command.builder.arguments.ArgumentStringArray
import net.minestom.server.command.builder.suggestion.SuggestionEntry
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.createType
import kotlin.reflect.jvm.isAccessible

data class MinestomContext(
    val node: CommandNode<Command>,
    val sender: CommandSender,
    val label: String,
    val input: String
)

class CommandInteraction(
    override val commando: MinestomCommando
) : StringCommandInteractionType<CommandSender, MinestomContext, Command>(
    commando,
    CommandSender::class,
    Command::class
) {
    init {
        addContextCreator { _, context ->
            AliasContext(context.label)
        }
    }

    override fun testFunction(parent: KClass<*>, handle: FunctionHandle): Result<Nothing?> {
        if (isHasAccess(handle))
            return Result.success(null)

        return super.testFunction(parent, handle)
    }

    override fun createRootNode(parent: KClass<*>): Node<Command> {
        val command = parent.java.getAnnotation(Command::class.java)
        return CommandNode(commando, null, command.name, command)
    }

    override fun parse(root: Node<Command>, parent: KClass<*>, handle: FunctionHandle) {
        if (isHasAccess(handle)) {
            root.children += Node(commando, root, "<has-access>", handle.reflector)
            return
        }

        super.parse(root, parent, handle)
    }

    override fun execute(root: Node<Command>, context: MinestomContext): Result<Nothing?> {
        root as CommandNode<Command>
        val user = commando.userManager.getUser(context.sender)
        return execute(root, user, context.input, context)
    }

    override fun postParse(root: Node<Command>) {
        commando.logger.finest(root.toString())
        root as CommandNode<Command>
        val commandManager = MinecraftServer.getCommandManager()
        val commandInfo = root.context
        val command = createCommand(commandInfo, root)
        commandManager.register(command)
    }

    private fun isHasAccess(handle: FunctionHandle): Boolean {
        return handle.name == "hasAccess"
                && handle.parameters.map { it.first.type } == listOf(CommandSender::class.java, String::class.java)
                && handle.reflector.returnType == Boolean::class.createType()
    }

    private fun createCommand(command: Command, root: CommandNode<Command>): SimpleCommand {
        @Suppress("UNCHECKED_CAST")
        val hasAccess = root.children.find { it.name == "<has-access>" }?.context as KFunction<Boolean>?

        return object : SimpleCommand(command.name, *command.aliases) {
            val spaceRegex = Regex(" +")

            init {
                val regex = Regex("(?<=^| )\\x00")
                val params = syntaxes
                    .find { it.arguments.firstOrNull() is ArgumentStringArray }!!
                    .arguments[0] as ArgumentStringArray

                params.setSuggestionCallback { sender, context, suggestion ->
                    // for some reason, Minestom gives a null byte as input when the user hasn't
                    // supplied a parameter yet, so we need to remove it from our input.
                    val input = context.input.replace(spaceRegex, " ")
                        .substringAfter(' ', "")
                        .replace(regex, "")
                    val user = commando.userManager.getUser(sender)

                    for (completion in root.autoComplete(user, input))
                        suggestion.addEntry(SuggestionEntry(completion))
                }
            }

            override fun process(sender: CommandSender, command: String, args: Array<out String>): Boolean {
                val input = args.joinToString(" ")
                    .replace(spaceRegex, " ")

                val context = MinestomContext(root, sender, command, input)
                val executionResult = execute(root, context)

                if (executionResult.isFailure) {
                    val exception = executionResult.exceptionOrNull()!!

                    if (exception is ExecutionError.BadInput)
                        context.sender.sendMessage(Component
                            .text("Invalid usage: ${exception.message}")
                            .color(NamedTextColor.RED))
                    else {
                        context.sender.sendMessage(Component
                            .text("An error occurred executing the command. If you're a server administrator, please check logs.")
                            .color(NamedTextColor.RED))

                        commando.logger.severe("""
                            An error occurred executing the command '/$command $input':
                            ${exception.message}
                        """.trimIndent())
                    }
                }

                return true
            }

            override fun hasAccess(sender: CommandSender, input: String?): Boolean {
                hasAccess?.isAccessible = true
                return hasAccess?.call(sender, input)
                    ?: commando.canAccessBlock(sender, input, command.permission)
            }
        }
    }
}

