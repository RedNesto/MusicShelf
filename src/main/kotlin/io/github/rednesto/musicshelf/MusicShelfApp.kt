package io.github.rednesto.musicshelf

import io.github.rednesto.musicshelf.ui.scenes.MainShelfController
import io.github.rednesto.musicshelf.utils.configureFxmlLoader
import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import java.nio.file.Paths

class MusicShelfApp : Application() {

    override fun init() {
        app = this

        val mainShelfPath = Paths.get("").toAbsolutePath()
        mainShelf = Shelf(mainShelfPath).apply(Shelf::load)
    }

    override fun start(primaryStage: Stage) {
        primaryStage.apply {
            title = "MusicShelf"
            val fxmlLoader = configureFxmlLoader("/ui/scenes/MainShelf.fxml", resources = MusicShelfBundle.getBundle())
            fxmlLoader.setControllerFactory { MainShelfController(mainShelf) }
            scene = Scene(fxmlLoader.load())
            show()
        }
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
