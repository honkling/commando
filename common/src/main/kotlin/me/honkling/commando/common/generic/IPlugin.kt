package me.honkling.commando.common.generic

interface IPlugin<T> {
    fun get(): T

    fun error(message: String)
    fun warn(message: String)
}