package me.honkling.commando.commands

import me.honkling.commando.CommandManager
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class CommandCompleter(val manager: CommandManager) : TabCompleter {
    override fun onTabComplete(
        sender: CommandSender,
        rawCommand: Command,
        label: String,
        rawArgs: Array<out String>
    ): MutableList<String> {
        val args = rawArgs.toList()
        val completions = mutableListOf<String>()
        val command = manager.commands[rawCommand.name] ?: return completions

        if (args.size <= 1)
            completions.addAll(command.keys.filter { it != command.name && it.contains(if (args.isEmpty()) "" else args[0]) })

        val (method, parsedArgs) = command.getMethod(command, args) ?: return completions
        val restArgs = args.subList(args.size - parsedArgs.filterNotNull().size, args.size)

        if (restArgs.isNotEmpty()) {
            val param = method.parameters[restArgs.size].type
            val type = manager.types[param] ?: return completions
            completions.addAll(type.complete(restArgs.last()))
        }

        return completions
    }
}