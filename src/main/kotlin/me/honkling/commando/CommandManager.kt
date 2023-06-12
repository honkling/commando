package me.honkling.commando

import me.honkling.commando.annotations.Command
import me.honkling.commando.annotations.Nullable
import me.honkling.commando.commands.CCommand
import me.honkling.commando.commands.Subcommand
import me.honkling.commando.types.Type
import me.honkling.commando.types.impl.*
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.SimpleCommandMap
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.lang.reflect.Method
import java.lang.reflect.Modifier

class CommandManager(val instance: JavaPlugin) {
    private val commandMap = hookCommandMap()
    val commands = mutableMapOf<String, CCommand>()
    val types = mutableMapOf<Class<*>, Type<*>>(
        java.lang.Boolean::class.java to BooleanType,
        Integer::class.java to IntegerType,
        java.lang.Double::class.java to DoubleType,
        String::class.java to StringType,
        OfflinePlayer::class.java to OfflinePlayerType,
        Player::class.java to PlayerType
    )
    
    fun registerCommands(pkg: String) {
        for (clazz in scanPackage(pkg) { it.isAnnotationPresent(Command::class.java) }) {
            val parsedCommand = parseCommand(clazz)
            val command = parsedCommand.createCommand(instance)
            commands[parsedCommand.name] = parsedCommand
            
            commandMap.register(instance.name, command)
        }
    }
    
    fun parseCommand(clazz: Class<*>): CCommand {
        val anno = clazz.getAnnotation(Command::class.java) ?: Command(
            clazz.name.split("$").last().lowercase(),
            "")

        val command = CCommand(this,
            clazz,
            anno.name.lowercase(),
            anno.aliases.toList(),
            anno.description,
            anno.usage,
            anno.permission,
            anno.permissionMessage)

        for (subclass in clazz.classes) {
            val name = subclass.name.split("$").last().lowercase()

            command.putIfAbsent(name, mutableListOf())
            command[name]!!.add(parseCommand(subclass))
        }
        
        for (method in clazz.declaredMethods.filter { Modifier.isPublic(it.modifiers) }) {
            val name = method.name.lowercase()
            
            if (!Modifier.isStatic(method.modifiers))
                throw IllegalStateException("Found a public non-static method (${name}). Please add static or make it private")

            command.putIfAbsent(name, mutableListOf())
            command[name]!!.add(parseSubcommand(method))
        }
        
        return command
    }
    
    fun parseSubcommand(method: Method): Subcommand {
        val subcommand = Subcommand(method)
        val parameters = method.parameters.slice(1 until method.parameters.size)
        var foundOptional = false
        
        for (parameter in parameters) {
            val type = Class.forName(parameter
                    .type
                    .name
                    .replace("byte", "java.lang.Byte")
                    .replace("long", "java.lang.Long")
                    .replace("int", "java.lang.Integer")
                    .replace("short", "java.lang.Short")
                    .replace("float", "java.lang.Float")
                    .replace("double", "java.lang.Double")
                    .replace("char", "java.lang.Character")
                    .replace("boolean", "java.lang.Boolean"))
            
            val isRequired = parameter.annotations.none { "Nullable" in (it.annotationClass.qualifiedName ?: "") }

            if (foundOptional && isRequired)
                throw IllegalStateException("Invalid optional arguments")

            if (!isRequired)
                foundOptional = true;

            if (type !in types)
                throw IllegalArgumentException("Found a subcommand (${method.name.lowercase()}) using an invalid type (${type})")
            
            subcommand.add(Triple(parameter.name, types[type]!!, isRequired))
        }
        
        return subcommand
    }
    
    private fun hookCommandMap(): SimpleCommandMap {
        val server = Bukkit.getServer()
        val getCommandMap = server.javaClass.getDeclaredMethod("getCommandMap")
        return getCommandMap.invoke(server) as SimpleCommandMap
    }
}