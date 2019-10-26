package io.github.rednesto.musicshelf.utils

import java.nio.file.Path

fun getItemNameForPath(path: Path): String = path.fileName.toString().substringBeforeLast('.')

fun normalizeGroup(group: String): String {
    fun isSuperfluousChar(c: Char) = c == '/' || c.isWhitespace()

    val trimmedGroup = group.trim(::isSuperfluousChar)
    if (trimmedGroup.isBlank()) {
        return "/"
    }

    val builder = StringBuilder(trimmedGroup.length)
    for (i in trimmedGroup.indices) {
        val c = trimmedGroup[i]

        if (c == '/') {
            // We are at a separator...
            if (builder.lastOrNull() == '/') {
                // but we just entered a new group with the last char, let's just ignore this one
                continue
            }

            // this is not a duplicate separator, so we need to remove any whitespaces between the group name and this char
            val lastIndexToKeep = builder.indexOfLast { !isSuperfluousChar(it) }
            builder.delete(lastIndexToKeep + 1, builder.length)
        } else if (c.isWhitespace() && builder.lastOrNull() == '/') {
            // We do not add whitespaces between last separator and next group name
            continue
        }

        builder.append(c)
    }

    return builder.toString()
}

fun normalizeGroups(groups: List<String>): List<String> = groups.mapTo(ArrayList(groups.size), ::normalizeGroup)
