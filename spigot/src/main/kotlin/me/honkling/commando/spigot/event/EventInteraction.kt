package me.honkling.commando.spigot.event

import me.honkling.commando.common.node.Node
import me.honkling.commando.common.parser.InteractionType
import me.honkling.commando.common.parser.handle.FunctionHandle
import me.honkling.commando.spigot.SpigotCommando
import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.plugin.EventExecutor
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.jvmName
import org.bukkit.event.Listener as EventListener

class EventInteraction(
    private val commando: SpigotCommando
) : InteractionType<Event, Unit, Unit>() {
    private class ListenerImpl : EventListener

    override fun testParent(parent: KClass<*>): Result<Unit> {
        if (parent.java.annotations.none { it.annotationClass == Listener::class })
            return Result.failure(IllegalStateException("Class doesn't have a listener annotation"))

        return Result.success(Unit)
    }

    override fun testFunction(parent: KClass<*>, handle: FunctionHandle): Result<Unit> {
        val reflector = handle.reflector

        if (reflector.parameters.size != 1)
            return Result.failure(IllegalStateException("Function '${reflector.name}' needs exactly one parameter"))

        if (!reflector.javaMethod!!.parameterTypes.first().kotlin.isSubclassOf(Event::class))
            return Result.failure(IllegalStateException("Function '${reflector.name}' doesn't have an Event as the first parameter."))

        return Result.success(Unit)
    }

    override fun createRootNode(parent: KClass<*>): Node<Unit> {
        return Node(null, parent.jvmName, Unit)
    }

    override fun parse(root: Node<Unit>, parent: KClass<*>, handle: FunctionHandle) {
        @Suppress("UNCHECKED_CAST")
        val eventType = handle.parameters[0].first.type.kotlin as KClass<out Event>
        val priority = handle.reflector.findAnnotation<Priority>()
        val node = Node(root, handle.name, EventContext(
            handle.reflector,
            eventType,
            priority?.priority ?: EventPriority.NORMAL
        ))
        root.children += node
    }

    override fun execute(root: Node<Unit>, context: Unit): Result<Unit> {
        return Result.failure(IllegalStateException("Execute should not be called on EventInteraction"))
    }

    override fun postParse(root: Node<Unit>) {
        val pluginManager = Bukkit.getPluginManager()

        @Suppress("UNCHECKED_CAST")
        for (node in root.children as List<Node<EventContext>>) {
            val (function, eventType, priority) = node.context
            val listener = ListenerImpl()

            pluginManager.registerEvent(eventType.java, listener, priority, EventExecutor { _, event ->
                function.isAccessible = true
                function.call(event)
            }, commando.plugin)
        }
    }
}