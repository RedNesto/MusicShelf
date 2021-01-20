package io.github.rednesto.musicshelf.acrobatreader

import io.github.rednesto.musicshelf.appSupport.builtin.ExecutableBasedAppSupport
import java.nio.file.Path
import java.util.*

class AcrobatReaderAppSupport : ExecutableBasedAppSupport() {

    override val id: String = "acrobat-reader"

    override fun getDisplayname(locale: Locale): String = "Acrobat Reader"

    override fun supports(file: Path): Boolean = super.supports(file) && file.fileName.toString().endsWith(".pdf")
}
