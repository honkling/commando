package me.honkling.commando.common.platform

abstract class User<Accessor : Any>(val accessor: Accessor) {
    abstract val displayName: String
    abstract val identifier: String
}