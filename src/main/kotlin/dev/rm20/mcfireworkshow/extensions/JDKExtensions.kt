package dev.rm20.mcfireworkshow.extensions

import dev.rm20.mcfireworkshow.MCFireworkShow
import org.slf4j.LoggerFactory

fun getLogger(): org.slf4j.Logger {
    return LoggerFactory.getLogger(MCFireworkShow::class.java)
}

fun <T : Any> T.nullIf(condition: (T) -> Boolean): T? {
    return if (condition(this)) null else this
}