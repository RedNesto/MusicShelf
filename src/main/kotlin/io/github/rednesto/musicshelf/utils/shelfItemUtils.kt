package io.github.rednesto.musicshelf.utils

import java.nio.file.Path

fun getItemNameForPath(path: Path): String = path.fileName.toString().substringBeforeLast('.')

fun isRootGroup(group: String): Boolean = group == "/"

fun normalizeGroup(group: String): String {
    val trimmedGroup = group.trim()
    if (isRootGroup(trimmedGroup)) {
        return trimmedGroup
    }

    return trimmedGroup.trim('/')
}

fun normalizeGroups(groups: List<String>): List<String> = groups.mapTo(ArrayList(groups.size), ::normalizeGroup)
