package me.honkling.commando.common

import java.io.File
import java.util.function.BiConsumer
import java.util.jar.JarEntry
import java.util.jar.JarInputStream

fun scanJar(instanceClass: Class<*>, predicate: (String) -> Boolean, callback: BiConsumer<JarEntry, String>) {
    val path = instanceClass.protectionDomain.codeSource.location.toURI()
    val jar = File(path)
    val stream = JarInputStream(jar.inputStream())

    while (true) {
        val entry = stream.nextJarEntry ?: break
        val entryName = entry.name

        if (!predicate.invoke(entryName))
            continue

        callback.accept(entry, entryName)
    }
}

fun getClassesInPackage(instanceClass: Class<*>, pkg: String, predicate: (Class<*>) -> Boolean = { true }): List<Class<*>> {
    val classes = mutableListOf<Class<*>>()
    val directory = pkg.replace('.', '/')

    scanJar(instanceClass, { n -> n.startsWith(directory) && n.endsWith(".class") && "$" !in n }) { _, entryName ->
        val clazz = instanceClass.classLoader.loadClass(entryName
                .replace('/', '.')
                .replace(".class", ""))

        if (!predicate.invoke(clazz))
            return@scanJar

        classes.add(clazz)
    }

    return classes
}