# What is Commando?

Commando is a completely automatic annotation-based Kotlin command framework for Bukkit.

# Table of Contents
- [What is Commando?](#what-is-commando)
- [Installation](#installation)
- [Usage](#usage)
    - [Setup](#setup)
    - [Creating Commands](#creating-commands)
    - [Command Arguments](#command-arguments)
    - [Command Annotation](#command-annotation)
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
    <groupId>com.github.honkling.commando</groupId>
    <artifactId>spigot</artifactId> # Replace with your platform
    <version>COMMIT-SHA</version> # Replace with latest commit
</dependency>
```
</details>

<details>
    <summary>Gradle</summary>

```groovy
dependencies {
    // Replace `spigot` with your platform and `COMMIT-SHA` with the latest commit
    implementation 'com.github.honkling.commando:spigot:COMMIT-SHA'
}
```
</details>

# Usage

## Setup

When your plugin enables, you can register commands using the CommandManager.<br>
Supply the package containing all your commands, and you're good to go.

<details>
  <summary>Java</summary>

```java
@Override
public void onEnable() {
    // Replace with manager for your platform
    SpigotCommandManager commandManager = new SpigotCommandManager(this);
    commandManager.registerCommands("me.honkling.example.commands");
}
```
</details>
<details>
  <summary>Kotlin</summary>

```kt
override fun onEnable() {
    // Replace with manager for your platform
    val commandManager = SpigotCommandManager(this)
    commandManager.registerCommands("me.honkling.example.commands")
}
```
</details>

## Creating Commands

You can create a command just by defining the command name and a default method.

<details>
<summary>Java</summary>

```java

package me.honkling.example.commands;

import me.honkling.commando.common.annotations.Command;

@Command("example")
public class Example {
    public static void example(Player executor) {
        executor.sendMessage("hello world!");
    }
}
```
</details>
<details>
<summary>Kotlin</summary>

```kt
@file:Command("example")

package me.honkling.example.commands

import me.honkling.commando.common.annotations.Command

fun example(executor: Player) {
    executor.sendMessage("hello world!")
}
```
</details>

Notice how you don't have to tell the command handler that the method is the default.<br>
Commando registers all public methods of this file as a subcommand. The method named identically to the command will be chosen as the default.<br>
Thusly, if you defined another method as so:

<details>
<summary>Java</summary>

```java
public static void test(Player executor) {
    executor.sendMessage("Test passing");
}
```
</details>
<details>
<summary>Kotlin</summary>

```kt
fun test(executor: Player) {
    executor.sendMessage("Test passing")
}
```
</details>

This method will run when `/example test` is executed.

## Command Arguments

Commando will derive the command arguments by the function parameters.

<details>
<summary>Java</summary>

```java
// This command is defined by Commando as /example test (player)
public static void test(Player executor, Player target) {
    target.sendMessage("Hello from ${executor.name}!");
    executor.sendMessage("Said hello to ${target.name}!");
}
```
</details>
<details>
<summary>Kotlin</summary>

```kt
// This command is defined by Commando as /example test (player)
fun test(executor: Player, target: Player) {
    target.sendMessage("Hello from ${executor.name}!")
    executor.sendMessage("Said hello to ${target.name}!")
}
```
</details>

You can also make parameters optional by marking them as nullable.<br>
Do note, if this does not appear to work in-game, Commando provides its own Optional annotation that will work fine.
<details>
<summary>Java</summary>

```java
public static void test(Player executor, Player target, @Nullable int target) {
    target.sendMessage("Hello from ${executor.name}! (x${amount ?: 1})")
    executor.sendMessage("Said hello to ${target.name}!")
}
```
</details>
<details>
<summary>Kotlin</summary>

```kt
fun test(executor: Player, target: Player, amount: Int?) {
    target.sendMessage("Hello from ${executor.name}! (x${amount ?: 1})")
    executor.sendMessage("Said hello to ${target.name}!")
}
```
</details>

By default, Commando supports players, offline players, strings, integers, doubles and booleans.<br>
However, you can register your own types with the CommandManager.

Let's say we have a data class, `Example`, which accepts an integer and a string.<br>
We can write a type parser for this, as so:
<details>
<summary>Java</summary>

```java
package me.honkling.example.types;

import me.honkling.commando.common.generic.ICommandSender;
import me.honkling.commando.common.types.Type;
import me.honkling.example.lib.Example;

public class ExampleType implements Type<Example> {
  // Takes an input string, parses it, and returns an Example
  // NOTE: 'input' is the rest of the args concatenated together, in case you want to occupy multiple arguments.
  @Override
  public Example match(ICommandSender<?> sender, String input) {
    String[] chunks = input.split(" ")[0].split(":");
    int integer = Integer.parseInt(chunks[0]);
    String str = chunks[1];

    return Example(integer, str);
  }

  // Tests if an input could be parsed as an Example
  // NOTE: 'input' is the rest of the args concatenated together, in case you want to occupy multiple arguments.
  @Override
  public boolean matches(ICommandSender<?> sender, String input) {
    return input.matches("^(\\d+):\\S+");
  }

  // Returns a list of tab completions for Example
  @Override
  public List<String> complete(ICommandSender<?> sender, String input) {
    List<String> suggestions = new ArrayList<>();
    String first = input.split(" ")[0];
    
    if (!first.contains(":")) {
      suggestions.add(String.format("%s:", first));
      return suggestions;
    }

    return suggestions;
  }
}
```
</details>
<details>
<summary>Kotlin</summary>

```kt
package me.honkling.example.types

import me.honkling.commando.common.generic.ICommandSender
import me.honkling.commando.common.types.Type
import me.honkling.example.lib.Example

object ExampleType : Type<Example> {
    // Takes an input string, parses it, and returns an Example
    // NOTE: 'input' is the rest of the args concatenated together, in case you want to occupy multiple arguments.
    override fun match(sender: ICommandSender<*>, input: String): Example {
        val chunks = input.split(" ")[0].split(":")
        val int = chunks[0].toInt()
        val str = chunks[1]
        
        return Example(int, str)
    }
    
    // Tests if an input could be parsed as an Example
    // NOTE: 'input' is the rest of the args concatenated together, in case you want to occupy multiple arguments.
    override fun matches(sender: ICommandSender<*>, input: String): Boolean {
        return input.matches(Regex("^(\\d+):\\S+"))
    }
    
    // Returns a list of tab completions for Example
    override fun complete(sender: ICommandSender<*>, input: String): List<String> {
        val first = input.split(" ")[0]
        
        if (!first.contains(":"))
            return listOf("$first:")
        
        return emptyList()
    }
}
```
</details>

Then, we can register the type when our plugin enables.
<details>
<summary>Java</summary>

```kt
commandManager.getTypes().put(Example.class, new ExampleType());
```
</details>
<details>
<summary>Kotlin</summary>

```kt
commandManager.types[Example::class.java] = ExampleType
```
</details>

## Command Annotation

You can provide extra parameters to the Command annotation.<br>
This allows you to set permissions, usage messages, etc.<br>
Below shows all the parameters you can use.
```kt
@Target(AnnotationTarget.FILE)
annotation class Command(
  val name: String,
  vararg val aliases: String,
  val description: String = "A Commando command.",
  val usage: String = "Invalid usage. Please check /{0} help.", // {0} is substituted with the command name
  val permission: String = "commando.{0}", // {0} is substituted with the command name
  val permissionMessage: String = "You don't have permission to do that."
)
```

Here's an example command using these parameters.
```kt
@file:Command(
  "cake", // name
  "the-lie", "the-cake", // aliases
  description = "Gives a cake.",
  usage = "Invalid usage. /cake [player]",
  permission = "cakecore.cake",
  permissionMessage = "You need cakecore.cake to do that!"
)

package me.honkling.example.commands

import me.honkling.commando.common.annotations.Command
import org.bukkit.inventory.ItemStack
import org.bukkit.Material

fun cake(executor: Player, target: Player?) {
  val player = target ?: executor
  
  player.inventory.addItem(ItemStack(Material.CAKE))
  player.sendMessage("Here is your cake!")
}
```

# License

Commando uses the MIT license.<br>
Check out the [LICENSE file](./LICENSE) for further details.
