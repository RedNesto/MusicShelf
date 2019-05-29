package io.github.rednesto.musicshelf

import org.jetbrains.annotations.PropertyKey
import java.text.MessageFormat
import java.util.*

object MusicShelfBundle {

    fun getBundle(): ResourceBundle {
        return ResourceBundle.getBundle("/lang/musicshelf")
    }

    fun get(@PropertyKey(resourceBundle = "lang.musicshelf") key: String): String = getBundle().getString(key)

    fun get(@PropertyKey(resourceBundle = "lang.musicshelf") key: String, vararg arguments: String): String = MessageFormat.format(get(key), *arguments)
}
