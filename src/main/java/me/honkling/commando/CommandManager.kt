package me.honkling.commando

import io.github.classgraph.ClassGraph
import me.honkling.commando.lib.Command
import me.honkling.commando.lib.CommandCompletion
import me.honkling.commando.lib.Parameter
import me.honkling.commando.lib.Subcommand
import me.honkling.commando.types.Type
import me.honkling.commando.types.impl.*

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer
import org.bukkit.command.PluginCommand
import org.bukkit.command.SimpleCommandMap
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.lang.Math.max

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
		val scanner = ClassGraph()
				.enableAllInfo()
				.acceptPackages(pkg)
				.scan()

		scanner.getClassesWithAnnotation(Command::class.java).forEach { clazz ->
			val anno = clazz.annotationInfo.find { it.name.startsWith("me.honkling.commando.lib") }!!

			val name = (anno.parameterValues["name"].value as String).lowercase()
			val subcommands = mutableMapOf<String, List<Parameter>>()

			clazz.methodInfo.forEach { method ->
				if (method.isPrivate)
					return@forEach

				subcommands[method.name] = method
						.parameterInfo
						.toList()
						.subList(1, method.parameterInfo.size)
						.map { param ->
							val type = Class.forName(param
									.typeSignatureOrTypeDescriptor
									.toString()
									.replace("int", "java.lang.Integer")
									.replace("boolean", "java.lang.Boolean")
									.replace("char", "java.lang.Character"))

							val isRequired = !param.annotationInfo.map { it.name }.contains("org.jetbrains.annotations.Nullable")

							Pair(type, isRequired)
						}
			}

			val command = createCommand(anno.loadClassAndInstantiate() as Command)

			command.setExecutor { sender, _, _, args ->
				if (args.isEmpty() || !subcommands.containsKey(args[0].lowercase())) {
					if (!validateArguments(subcommands[name]!!, args))
						return@setExecutor false

					val method = clazz
							.getDeclaredMethodInfo(name)[0]
							.loadClassAndGetMethod()

					method.invoke(null, sender, *(parseArguments(subcommands[name]!!, args).toTypedArray()))
					return@setExecutor true
				}

				val subcommand = subcommands[args[0].lowercase()]!!
				val rest = args.toList().subList(1, args.size).toTypedArray()

				if (!validateArguments(subcommand, rest))
					return@setExecutor false

				val method = clazz
						.getDeclaredMethodInfo(args[0])[0]
						.loadClassAndGetMethod()

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