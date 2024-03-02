package me.honkling.commando.common.types

import me.honkling.commando.common.generic.ICommandSender

interface Type<T> {
    fun validate(sender: ICommandSender<*>, input: String): Boolean
    fun parse(sender: ICommandSender<*>, input: String): Pair<T, Int>
    fun complete(sender: ICommandSender<*>, input: String): List<String>
}