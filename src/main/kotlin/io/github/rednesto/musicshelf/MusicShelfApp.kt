package io.github.rednesto.musicshelf

import io.github.rednesto.musicshelf.utils.loadFxml
import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import java.nio.file.Paths
import java.util.*

class MusicShelfApp : Application() {

    override fun init() {
        app = this
    }

    override fun start(primaryStage: Stage) {
        primaryStage.scene = Scene(loadFxml("/ui/scenes/MainShelf.fxml", resources = MusicShelfBundle.getBundle()))
        primaryStage.show()
    }

    companion object {
        lateinit var app: MusicShelfApp
            private set

        @JvmStatic
        fun main(args: Array<String>) {
            launch(MusicShelfApp::class.java, *args)
        }
    }
}
