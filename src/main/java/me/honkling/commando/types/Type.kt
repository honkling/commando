package me.honkling.commando.types

interface Type<T> {
	fun match(input: String): T
	fun matches(input: String): Boolean
	fun complete(input: String): List<String>
}