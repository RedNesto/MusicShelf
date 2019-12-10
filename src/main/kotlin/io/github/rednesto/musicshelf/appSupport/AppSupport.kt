package io.github.rednesto.musicshelf.appSupport

import java.util.*

interface AppSupport {
    val id: String

    fun getDisplayname(locale: Locale): String
}
