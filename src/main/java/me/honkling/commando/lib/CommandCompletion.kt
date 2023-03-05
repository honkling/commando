package me.honkling.commando.lib

import me.honkling.commando.CommandManager
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class CommandCompletion(val manager: CommandManager) : TabCompleter {
	override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): MutableList<String> {
		val subcommands = manager.commands[command.name]!!
		val completions = mutableListOf<String>()
		val subcommand = if (args.isEmpty() || !subcommands.contains(args[0].lowercase())) command.name else args[0]

		if (args.isEmpty() || args[0] == "")
			completions.addAll(subcommands.keys.filter { it != command.name })

		val subArgs = args.toMutableList()
		if (subcommand != command.name) subArgs.removeAt(0)

		if (subArgs.size > 0) {
			val arg = subcommands[subcommand]!![subArgs.size - 1]
			val completer = manager.types[arg.first]!!
			completions.addAll(completer.complete(args.last()))
		}

		return completions
	}
}