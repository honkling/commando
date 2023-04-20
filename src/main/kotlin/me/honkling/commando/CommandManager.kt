package me.honkling.commando

import me.honkling.commando.lib.*
import me.honkling.commando.types.Type
import me.honkling.commando.types.impl.*

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer
import org.bukkit.command.PluginCommand
import org.bukkit.command.SimpleCommandMap
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.annotations.Nullable
import java.io.File
import java.lang.reflect.Modifier
import java.util.jar.JarInputStream

class CommandManager(private val instance: JavaPlugin) {
	private val completer = CommandCompletion(this)
	private val commandMap = hookCommandMap()
	val types = mutableMapOf<Class<*>, Type<*>>()
	val commands = mutableMapOf<String, Subcommand>()

	init {
		types[String::class.java] = StringType
		types[Int::class.java] = IntegerType
		types[Integer::class.java] = IntegerType
		types[Double::class.java] = DoubleType
		types[Boolean::class.java] = BooleanType
		types[Player::class.java] = PlayerType
		types[OfflinePlayer::class.java] = OfflinePlayerType
	}

	fun registerCommands(pkg: String) {
		val instanceClass = instance::class.java
		val path = instanceClass.protectionDomain.codeSource.location.toURI()
		val jar = File(path)
		val stream = JarInputStream(jar.inputStream())
		val directory = pkg.replace('.', '/')

		while (true) {
			val entry = stream.nextJarEntry ?: break
			val entryName = entry.name

			if (!entryName.startsWith(directory) || !entryName.endsWith(".class"))
				continue

			val clazz = instanceClass.classLoader.loadClass(entryName
				.replace('/', '.')
				.replace(".class", ""))

			if (!clazz.isAnnotationPresent(Command::class.java))
				continue

			val anno = clazz.getAnnotation(Command::class.java)!!
			val name = anno.name.lowercase()
			val subcommands = mutableMapOf<String, List<Parameter>>()

			clazz.methods.forEach { method ->
				if (!Modifier.isStatic(method.modifiers)) {
					CommandoLogger.warning("Found a public non-static subcommand. We didn't register this subcommand. Please add static or make it private.")
					return@forEach
				}

				subcommands[method.name] = method
					.parameters
					.toList()
					.subList(1, method.parameters.size)
					.map { param ->
						val type = Class.forName(
							param
								.type
								.name
								.replace("int", "java.lang.Integer")
								.replace("byte", "java.lang.Byte")
								.replace("short", "java.lang.Short")
								.replace("long", "java.lang.Long")
								.replace("float", "java.lang.Float")
								.replace("boolean", "java.lang.Boolean")
								.replace("char", "java.lang.Character")
								.replace("double", "java.lang.Double")
						)

						val isRequired = !param.isAnnotationPresent(Nullable::class.java)

						if (!isRequired && param.annotations.find { it::class.java.name.contains("Nullable") } != null)
							CommandoLogger.warning("Found a nullable-marked parameter not using JetBrains' Nullable. Did you import the wrong annotation?")

						if (!types.containsKey(type)) {
							CommandoLogger.warning("Found a parameter using a type that Commando doesn't know how to parse. We won't register this subcommand.")
							return@forEach
						}

						Pair(type, isRequired)
					}
			}

			val command = createCommand(anno)

			command.setExecutor { sender, _, _, args ->
				if (args.isEmpty() || !subcommands.containsKey(args[0].lowercase())) {
					if (!validateArguments(subcommands[name]!!, args))
						return@setExecutor false

					val method = clazz.methods.find { it.name == name } ?: return@setExecutor false

					method.invoke(null, sender, *(parseArguments(subcommands[name]!!, args).toTypedArray()))
					return@setExecutor true
				}

				val subcommand = subcommands[args[0].lowercase()]!!
				val rest = args.toList().subList(1, args.size).toTypedArray()

				if (!validateArguments(subcommand, rest))
					return@setExecutor false

				val method = clazz.methods.find { it.name == args[0] } ?: return@setExecutor false

				method.invoke(null, sender, *parseArguments(subcommand, rest).toTypedArray())
				return@setExecutor true
			}

			commands[name] = subcommands
			commandMap.register(instance.name, command)
		}
	}

	private fun validateArguments(guide: List<Pair<Class<*>, Boolean>>, args: Array<String>): Boolean {
		val requiredArgs = guide.filter { it.second }
		val optionalArgs = guide.filter { !it.second }
		val providedOArgs = args.size - requiredArgs.size

		// If there are fewer arguments than required arguments, this is invalid
		if (args.size < requiredArgs.size)
			return false

		// Validate all required arguments, if any
		requiredArgs.forEachIndexed { index, arg ->
			val type = types[arg.first]
				?: throw IllegalStateException("Found parameter type '${arg.first}' that Commando cannot parse.")

			if (type != null && !type.matches(args[index]))
				return false
		}

		// Validate all optional arguments, if any
		optionalArgs.forEachIndexed { index, arg ->
			val type = types[arg.first]

			if (index + 1 > providedOArgs)
				return true

			if (type != null && !type.matches(args[index + requiredArgs.size]))
				return false
		}

		return true
	}

	private fun parseArguments(guide: List<Parameter>, args: Array<String>): List<Any?> {
		val parsed = mutableListOf<Any>()

		guide.forEachIndexed { index, guideArg ->
			if (args.size - 1 < index)
				return@forEachIndexed

			val type = types[guideArg.first]!!
			parsed.add(type.match(args[index])!!)
		}

		return listOf(
			*parsed.toTypedArray(),
			*arrayOfNulls(0.coerceAtLeast(guide.size - args.size))
		)
	}

	private fun createCommand(anno: Command): PluginCommand {
		val name = anno.name
		val description = anno.description

		val constructor = PluginCommand::class.java.declaredConstructors[0]
		constructor.isAccessible = true
		val command = constructor.newInstance(name, instance) as PluginCommand

		command.description = description
		command.usage = ChatColor.translateAlternateColorCodes(
				'&',
				anno.usage.replace("{0}", name))
		command.aliases = anno.aliases.toMutableList()
		command.permission = anno.permission.replace("{0}", name)
		command.tabCompleter = completer
		command.permissionMessage = anno.permissionMessage

		return command
	}

	private fun hookCommandMap(): SimpleCommandMap {
		val server = Bukkit.getServer()
		val getCommandMap = server.javaClass.getDeclaredMethod("getCommandMap")
		return getCommandMap.invoke(server) as SimpleCommandMap
	}
}