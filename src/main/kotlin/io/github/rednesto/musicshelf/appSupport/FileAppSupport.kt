package io.github.rednesto.musicshelf.appSupport

import java.nio.file.Path

interface FileAppSupport : AppSupport {

    fun supports(file: Path): Boolean

    fun open(file: Path)
}
