package me.honkling.commando.common.platform

abstract class UserManager<Accessor : Any> {
    private val userCache = mutableMapOf<Accessor, User<Accessor>>()
    val allUsers
        get() = userCache.values

    fun getUser(accessor: Accessor) =
        userCache.computeIfAbsent(accessor) { createUser(it) }
    abstract fun createUser(accessor: Accessor): User<Accessor>
}