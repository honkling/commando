package me.honkling.commando.commands

import me.honkling.commando.types.Type
import java.lang.reflect.Method

class Subcommand(val method: Method) : ArrayList<Parameter>()