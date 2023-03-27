package me.honkling.commando.lib

import java.util.logging.LogRecord
import java.util.logging.Logger

object CommandoLogger : Logger("Commando", null) {
    override fun log(log: LogRecord) {
        log.message = "[Commando] ${log.message}"
        super.log(log)
    }
}