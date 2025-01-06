package me.honkling.commando.common.parser

import me.honkling.commando.common.node.Node
import me.honkling.commando.common.parser.handle.FunctionHandle
import kotlin.reflect.KClass

abstract class InteractionType<Context, RootContext> {
    val contexts = mutableMapOf<KClass<*>, ContextCreator<Context, *>>()

    /**
     * Tests the class if it is valid for this interaction type.
     */
    abstract fun testParent(parent: KClass<*>): Result<Nothing?>

    /**
     * Tests the function if it is valid for this interaction type.
     *
     * The parent of this function is guaranteed to be
     * valid as defined by the 'test parent' method.
     */
    abstract fun testFunction(parent: KClass<*>, handle: FunctionHandle): Result<Nothing?>

    /**
     * Creates the root node at the beginning of parsing.
     *
     * The parent is guaranteed to be valid
     * as defined by the 'test parent' method.
     */
    abstract fun createRootNode(parent: KClass<*>): Node<RootContext>

    /**
     * Takes a function and its parent class and parses it,
     * adding children as necessary to the provided root node.
     *
     * The parent and function are both guaranteed to be valid as
     * according to the 'test parent' & 'test function' methods.
     */
    abstract fun parse(root: Node<RootContext>, parent: KClass<*>, handle: FunctionHandle)

    /**
     * Takes the root node and context (typically an event, ex. an
     * event when a user runs a command) and executes the node.
     */
    abstract fun execute(root: Node<RootContext>, context: Context): Result<Nothing?>

    /**
     * Takes the complete root node and performs any required
     * processing to it.
     */
    abstract fun postParse(root: Node<RootContext>)

    inline fun <reified This : ContextProvider> addContextCreator(noinline block: ContextCreatorBlock<Context, This>) {
        val context = ContextCreator(This::class, block)
        contexts[This::class] = context
    }
}