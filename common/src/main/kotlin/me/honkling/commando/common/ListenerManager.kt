package me.honkling.knockffa.manager

import me.honkling.commando.common.annotations.Listener
import me.honkling.commando.common.generic.IPlugin
import me.honkling.commando.common.getClassesInPackage

abstract class ListenerManager<T>(val plugin: IPlugin<T>) {
    fun registerListeners(pkg: String) {
        val listeners = getClassesInPackage(plugin.get()!!::class.java, pkg, ::isListener)
        listeners.forEach(::registerClass)
    }

    abstract fun registerClass(clazz: Class<*>)
    abstract fun isEvent(clazz: Class<*>): Boolean

    private fun isListener(clazz: Class<*>): Boolean {
        return clazz.isAnnotationPresent(Listener::class.java)
    }
}