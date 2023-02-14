# What is Commando?

Commando is a completely automatic annotation-based Kotlin command framework for Bukkit.

# Table of Contents
- [What is Commando?](#what-is-commando)
- [Installation](#installation)
- [Usage](#usage)
  - [Setup](#setup)
  - [Creating Commands](#creating-commands)
  - [Command Arguments](#command-arguments)
- [License](#license)

# Installation

Commando is accessible via [Jitpack](https://jitpack.io). You can use it in your plugin by adding the Jitpack repository:

<details>
    <summary>Maven</summary>

```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```
</details>

<details>
    <summary>Gradle</summary>

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
```
</details>

After adding the repository, you can include Commando with the following:

<details>
    <summary>Maven</summary>

```xml
<dependency>
    <groupId>com.github.honkling</groupId>
    <artifactId>commando</artifactId>
    <version>1.0.0</version>
</dependency>
```
</details>

<details>
    <summary>Gradle</summary>

```groovy
dependencies {
    implementation 'com.github.honkling:commando:1.0.0'
}
```
</details>

# Usage

## Setup

When your plugin enables, you can register commands using the CommandManager.<br>
Supply the package containing all your commands, and you're good to go.
```kt
override fun onEnable() {
    val commandManager = CommandManager(this)
    commandManager.registerCommands("me.honkling.example.commands")
}
```

## Creating Commands

You can create a command just by defining the command name and a default method.
```kt
@file:Command("example")

package me.honkling.example.commands

import me.honkling.commando.lib.Command

fun example(executor: Player) {
    executor.sendMessage("hello world!")
}
```

Notice how you don't have to tell the command handler that the method is the default.<br>
Commando registers all public methods of this file as a subcommand. The method named identically to the command will be chosen as the default.<br>
Thusly, if you defined another method as so:
```kt
fun test(executor: Player) {
    executor.sendMessage("Test passing")
}
```

This method will run when `/example test` is executed.

## Command Arguments

Commando will derive the command arguments by the function parameters.
```kt
// This command is defined by Commando as /example test (player)
fun test(executor: Player, target: Player) {
    target.sendMessage("Hello from ${executor.name}!")
    executor.sendMessage("Said hello to ${target.name}!")
}
```

You can also make parameters optional with the nullable operator.
```kt
fun test(executor: Player, target: Player, amount: Int?) {
    target.sendMessage("Hello from ${executor.name}! (x${amount ?: 1})")
    executor.sendMessage("Said hello to ${target.name}!")
}
```

By default, Commando supports players, offline players, strings, integers, doubles and booleans.<br>
However, you can register your own types with the CommandManager.

Let's say we have a data class, `Example`, which accepts an integer and a string.<br>
We can write a type parser for this, as so:
<details>
<summary>ExampleType</summary>

```kt
package me.honkling.example.types

import me.honkling.example.lib.Example

object ExampleType : Type<Example> {
    // Takes an input string, parses it, and returns an Example
    override fun match(input: String): Example {
        val chunks = input.split(":")
        val int = chunks[0].toInt()
        val str = chunks[1]
        
        return Example(int, str)
    }
    
    // Tests if an input could be parsed as an Example
    override fun matches(input: String): Boolean {
        return input.matches(Regex("^(\d+):.+"))
    }
    
    // Returns a list of tab completions for Example
    override fun complete(input: String): List<String> {
        if (!input.contains(":"))
            return listOf("$input:")
        
        return emptyList()
    }
}
```
</details>

Then, we can register the type when our plugin enables.
```kt
commandManager.types[Example::class.java] = ExampleType
```

# License

MIT License

Copyright (c) 2023 honkling

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
