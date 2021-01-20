package io.github.rednesto.musicshelf.musescore

import io.github.rednesto.musicshelf.appSupport.builtin.ExecutableBasedAppSupport
import java.nio.file.Path
import java.util.*

class Musescore2AppSupport : ExecutableBasedAppSupport() {

    override val id: String = "musescore2"

    override fun getDisplayname(locale: Locale): String = "Musescore 2"

    override fun supports(file: Path): Boolean = super.supports(file) && file.fileName.toString().endsWith(".mscz")
}
