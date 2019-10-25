package io.github.rednesto.musicshelf.utils

import java.nio.file.Path

fun getItemNameForPath(path: Path): String = path.fileName.toString().substringBeforeLast('.')
