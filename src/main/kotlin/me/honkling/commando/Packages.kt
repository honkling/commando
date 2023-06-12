package me.honkling.commando

import java.io.File
import java.util.jar.JarInputStream

fun scanPackage(
    pkg: String,
    predicate: (Class<*>) -> Boolean = { true }
): List<Class<*>> {
    val instance = Commando::class.java
    val uri = instance.protectionDomain.codeSource.location.toURI()
    val jar = File(uri)
    val stream = JarInputStream(jar.inputStream())
    val directory = pkg.replace('.', '/')
    val classes = mutableListOf<Class<*>>()
    
    while (true) {
        val entry = stream.nextJarEntry ?: break
        val name = entry.name
        
        if (!name.startsWith(directory) || !name.endsWith(".class"))
            continue
        
        val clazz = instance.classLoader.loadClass(name
            .replace('/', '.')
            .replace(".class", ""))
        
        if (predicate.invoke(clazz))
            classes.add(clazz)
    }
    
    return classes
}

