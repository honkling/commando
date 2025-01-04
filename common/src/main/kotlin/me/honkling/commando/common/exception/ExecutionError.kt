package me.honkling.commando.common.exception


open class ExecutionError(message: String) : Exception(message) {
    class BadInput(message: String) : ExecutionError(message)
}