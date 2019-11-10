package io.github.rednesto.musicshelf.ui

import io.github.rednesto.musicshelf.MusicShelfBundle
import io.github.rednesto.musicshelf.Shelf
import io.github.rednesto.musicshelf.ui.scenes.ShelfViewController
import io.github.rednesto.musicshelf.utils.configureFxmlLoader
import javafx.scene.Scene
import javafx.stage.Stage

object ShelfViewWindow {
    fun create(shelf: Shelf, stage: Stage = Stage()) = stage.apply {
        title = "MusicShelf - ${shelf.name}"
        val fxmlLoader = configureFxmlLoader("/ui/scenes/ShelfView.fxml", resources = MusicShelfBundle.getBundle())
        fxmlLoader.setControllerFactory { ShelfViewController(shelf) }
        scene = Scene(fxmlLoader.load())
    }
}
