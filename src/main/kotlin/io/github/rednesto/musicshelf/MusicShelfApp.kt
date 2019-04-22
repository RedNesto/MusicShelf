package io.github.rednesto.musicshelf

import javafx.application.Application
import javafx.stage.Stage

class MusicShelfApp : Application() {

    override fun init() {
        app = this
    }

    override fun start(primaryStage: Stage) {
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
