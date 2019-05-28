package io.github.rednesto.musicshelf.utils

import java.awt.Desktop
import java.nio.file.Path

object DesktopHelper {
    fun open(filePath: Path) = this { desktop: Desktop ->
        desktop.open(filePath.toFile())
    }

    operator fun invoke(action: (desktop :Desktop) -> Unit) {
        if (Desktop.isDesktopSupported()) {
            action(Desktop.getDesktop())
        }
    }
}
