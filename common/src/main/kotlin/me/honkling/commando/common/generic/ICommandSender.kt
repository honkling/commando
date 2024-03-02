package me.honkling.commando.common.generic

interface ICommandSender<T> {
    fun get(): T
}