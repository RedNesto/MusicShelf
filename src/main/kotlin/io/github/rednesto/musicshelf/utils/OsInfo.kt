package io.github.rednesto.musicshelf.utils

import java.util.*

object OsInfo {

    private val OS_NAME_LOWER = System.getProperty("os.name").toLowerCase(Locale.ENGLISH)

    val IS_WINDOWS = OS_NAME_LOWER.startsWith("windows")
    val IS_MACOS = OS_NAME_LOWER.startsWith("mac")
}
