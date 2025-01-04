package me.honkling.commando.common.node

import me.honkling.commando.common.platform.User

interface AutoCompletable {
    fun autoComplete(user: User<*>, input: String): List<String>
}