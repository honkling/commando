package me.honkling.commando.spigot.event

import com.github.retrooper.packetevents.event.PacketListener
import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.protocol.chat.Node
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDeclareCommands
import me.honkling.commando.common.command.node.CommandNode
import me.honkling.commando.common.command.node.SubCommandNode
import me.honkling.commando.spigot.SpigotCommando
import org.bukkit.Bukkit
import org.bukkit.command.PluginCommand
import kotlin.experimental.and
import kotlin.jvm.optionals.getOrNull
import me.honkling.commando.common.node.Node as CommandoNode

private const val literalFlagMask = 0x1.toByte()
private const val argumentFlagMask = 0x2.toByte()
private const val hasRedirectMask = 0x08.toByte()

class Packets(private val commando: SpigotCommando) : PacketListener {
    override fun onPacketSend(event: PacketSendEvent) {
        if (event.packetType != PacketType.Play.Server.DECLARE_COMMANDS)
            return

        val commandMap = Bukkit.getCommandMap()
        val packet = WrapperPlayServerDeclareCommands(event)
        val nodes = packet.nodes
        val root = nodes[packet.rootIndex]

        for (index in root.children) {
            val literal = nodes[index]

            if (literal.flags.and(hasRedirectMask) != 0.toByte())
                continue

            val name = literal.name.getOrNull()
                ?: continue

            val command = commandMap.knownCommands[name]
                ?: continue

            if (command is PluginCommand && command.plugin == commando.plugin) {
                val parseResult = commando.parsedNodes.find { it.node.name == command.name }
                    ?: continue // Not a command registered by commando, we can ignore it

                literal.children.clear()
                processCommand(packet, literal, parseResult.node)
            }
        }
    }

    private fun processCommand(packet: WrapperPlayServerDeclareCommands, treeNode: Node, node: CommandoNode<*>) {
        val literalIndexes = mutableMapOf<String, Int>()

        for (child in node.children) {
            val name = child.name

            if (child is CommandNode<*> || child is SubCommandNode<*>) {
                var literalNode = literalIndexes[name]?.let { packet.nodes[it] }

                if (literalNode == null) {
                    literalNode = Node(literalFlagMask, mutableListOf(), 0, name, null as Int?, null, null)
                    packet.nodes.add(literalNode)
                }

                processCommand(packet, literalNode, child)
                continue
            }


        }
    }

    private fun isNodeExecutable(node: CommandoNode<*>): Boolean {
        return node.children.any { it.is }
    }
}