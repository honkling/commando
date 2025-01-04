package me.honkling.commando.common

import me.honkling.commando.common.lib.getClassesInPackage
import me.honkling.commando.common.node.Node
import me.honkling.commando.common.parser.ClassParseResult
import me.honkling.commando.common.parser.InteractionRegistry
import me.honkling.commando.common.parser.parseClass
import me.honkling.commando.common.platform.UserManager
import me.honkling.commando.common.type.BooleanType
import me.honkling.commando.common.type.NumberType
import me.honkling.commando.common.type.StringType
import me.honkling.commando.common.type.TypeRegistry
import java.util.logging.Logger

abstract class Commando {
    val logger: Logger = Logger.getLogger("commando")
    val interactionRegistry = InteractionRegistry()
    val typeRegistry = TypeRegistry()
    abstract val userManager: UserManager<*>

    @Suppress("MemberVisibilityCanBePrivate")
    val parsedNodes = mutableListOf<ClassParseResult>()

    init {
        typeRegistry.register(StringType(), String::class)
        typeRegistry.register(NumberType(Byte::class), Byte::class, java.lang.Byte::class)
        typeRegistry.register(NumberType(Short::class), Short::class, java.lang.Short::class)
        typeRegistry.register(NumberType(Int::class), Int::class, java.lang.Integer::class)
        typeRegistry.register(NumberType(Float::class), Float::class, java.lang.Float::class)
        typeRegistry.register(NumberType(Double::class), Double::class, java.lang.Double::class)
        typeRegistry.register(BooleanType(), Boolean::class, java.lang.Boolean::class)
    }

    fun register(rootPackage: String, vararg children: String) {
        val classes = children
            .map { getClassesInPackage(Commando::class.java, "$rootPackage.$it") }
            .flatten()

        for (clazz in classes) {
            println("Parsing class: ${clazz.name}")
            val parseResult = parseClass(this, clazz.kotlin)

            if (parseResult.isFailure) {
                println("Failed: ${parseResult.exceptionOrNull()!!.message}")
                continue
            }

            val classResult = parseResult.getOrThrow()
            parsedNodes += classResult
            classResult.interactionType::postParse.call(classResult.node)
        }
    }
}