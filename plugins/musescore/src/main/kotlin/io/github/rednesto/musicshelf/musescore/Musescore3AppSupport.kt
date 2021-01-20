package io.github.rednesto.musicshelf.musescore

import io.github.rednesto.musicshelf.appSupport.builtin.ExecutableBasedAppSupport
import java.nio.file.Path
import java.util.*

class Musescore3AppSupport : ExecutableBasedAppSupport() {

    override val id: String = "musescore3"

    override fun getDisplayname(locale: Locale): String = "Musescore 3"

    override fun supports(file: Path): Boolean = super.supports(file) && file.fileName.toString().endsWith(".mscz")
}
