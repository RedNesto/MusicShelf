package io.github.rednesto.musicshelf

import io.github.rednesto.musicshelf.ui.ShelfViewWindow
import javafx.application.Application
import javafx.stage.Stage
import java.nio.file.Paths

class MusicShelfApp : Application() {

    override fun init() {
        app = this

        // TODO support multiple shelves
        val mainShelfPath = Paths.get("").toAbsolutePath()
        mainShelf = Shelf("Main Shelf", mainShelfPath).apply(Shelf::load)
    }

    override fun start(primaryStage: Stage) {
        ShelfViewWindow.create(mainShelf, primaryStage).show()
    }

    override fun stop() {
        mainShelf.save()
    }

    companion object {
        lateinit var app: MusicShelfApp
            private set

        lateinit var mainShelf: Shelf
            private set

        @JvmStatic
        fun main(args: Array<String>) {
            launch(MusicShelfApp::class.java, *args)
        }
    }
}
