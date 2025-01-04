package me.honkling.commando.common.node

import me.honkling.commando.common.platform.User

interface Parsable<ParseResult : Any> {
    fun parse(user: User<*>, input: String): Result<ParseResult>
}