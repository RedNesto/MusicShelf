package io.github.rednesto.musicshelf.utils

import java.awt.Desktop
import java.nio.file.Files
import java.nio.file.Path

object DesktopHelper {
    fun open(filePath: Path) {
        if (Files.notExists(filePath)) {
            return
        }

        this { desktop: Desktop ->
            desktop.open(filePath.toFile())
        }
    }

    fun show(filePath: Path) {
        if (Files.notExists(filePath)) {
            return
        }

        this {
            val absolutePath = filePath.toAbsolutePath().toString()
            when {
                OsInfo.IS_WINDOWS -> Runtime.getRuntime().exec("explorer /select,\"$absolutePath\"")
                OsInfo.IS_MACOS -> Runtime.getRuntime().exec(arrayOf("open", "-R", absolutePath))
            }
        }
    }

    fun supportsShow(): Boolean = OsInfo.IS_WINDOWS || OsInfo.IS_MACOS

    operator fun invoke(action: (desktop: Desktop) -> Unit) {
        if (Desktop.isDesktopSupported()) {
            action(Desktop.getDesktop())
        }
    }
}
