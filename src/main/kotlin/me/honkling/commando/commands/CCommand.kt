package me.honkling.commando.commands

import me.honkling.commando.CommandManager
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.command.PluginCommand
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.lang.reflect.Method

class CCommand(
    val manager: CommandManager,
    val clazz: Class<*>,
    val name: String,
    var aliases: List<String>,
    val description: String = "A Commando command.",
    val usage: String = "Invalid usage. Please check /{0} help.",
    val permission: String = "commando.{0}",
    val permissionMessage: String = "You don't have permission to do that."
) : HashMap<String, MutableList<Any>>() {
    fun createCommand(instance: JavaPlugin): PluginCommand {
        val constructor = PluginCommand::class.java.declaredConstructors[0]
        constructor.isAccessible = true
        val command = constructor.newInstance(name, instance) as PluginCommand
        
        command.description = description
        command.usage = ChatColor.translateAlternateColorCodes('&', usage.replace("{0}", name))
        command.aliases = aliases
        command.permission = permission.replace("{0}", name)
        command.permissionMessage = permissionMessage
        command.tabCompleter = CommandCompleter(manager)
        command.setExecutor(::executor)
        
        return command
    }
    
    fun executor(sender: CommandSender, command: Command, _label: String, args: Array<String>): Boolean {
        val (method, parsedArgs) = getMethod(sender, this, args.toList()) ?: return false
        val first = method.parameters[0].type

        if (first !in listOf(
            Player::class.java,
            ConsoleCommandSender::class.java,
            CommandSender::class.java
        ))
            throw IllegalStateException("Invalid first arg of '${method.name}' (must be Player, ConsoleCommandSender, or CommandSender)")

        if (!first.isAssignableFrom(sender::class.java))
            return false

        val returnValue = method.invoke(null, sender, *parsedArgs.toTypedArray())

        if (method.returnType == Boolean::class.java)
            return returnValue as Boolean
        
        return true
    }
    
    fun getMethod(sender: CommandSender, command: CCommand, args: List<String>): Pair<Method, List<Any?>>? {
        val section = (command[if (args.isNotEmpty()) args[0] else name] ?: command[name]) ?: return null

        for (candidate in section) {
            if (candidate is CCommand) {
                val method = getMethod(sender, candidate, args.subList(1, args.size))
                if (method != null) return method
            }

            if (candidate is Subcommand) {
                val method = candidate.method
                val parsedArgs = mutableListOf<Any?>()
                var validCandidate = true
                var index = 0

                if (candidate.filter { it.third }.size > args.size)
                    continue

                for (parameter in candidate) {
                    if (index >= args.size) {
                        if (parameter.third) {
                            validCandidate = false
                            break
                        }

                        parsedArgs.addAll(arrayOfNulls(candidate.size - args.size))
                        return method to parsedArgs
                    }

                    val restArgs = args.subList(index, args.size)
                    val typeHandler = parameter.second
                    val match = typeHandler.matches(sender, restArgs.joinToString(" "))

                    if (!match.matches) {
                        validCandidate = false
                        break
                    }

                    parsedArgs += typeHandler.match(sender, restArgs.joinToString(" "))!!
                    index += match.size
                }

                if (!validCandidate || index + (if (section === command[name]) 0 else 1) < args.size)
                    continue

                return method to parsedArgs
            }
        }
        
        return null
    }
}