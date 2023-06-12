package me.honkling.commando.types.impl

import me.honkling.commando.types.MatchResult
import me.honkling.commando.types.Type

object StringType : Type<String> {
	val regex = Regex("^(\"([^\"]|\\\\\")*\"|\\S+)")

	override fun match(input: String): String {
		val match = regex.find(input)!!.value

		if (match.startsWith("\"") && match.endsWith("\""))
			return match
				.substring(1, match.length - 1)
				.replace("\\\"", "\"")
				.replace("\\\\", "\\")

		return match
	}

	override fun matches(input: String): MatchResult {
		if (!regex.containsMatchIn(input))
			return MatchResult(false)

		val match = regex.find(input)!!.value
		return MatchResult(true, match.split(" ").size)
	}

	override fun complete(input: String): List<String> {
		return emptyList()
	}
}