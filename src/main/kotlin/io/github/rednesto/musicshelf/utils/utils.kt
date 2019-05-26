package io.github.rednesto.musicshelf.utils

infix fun Int.within(list: List<*>): Boolean = this >= 0 && this < list.size
