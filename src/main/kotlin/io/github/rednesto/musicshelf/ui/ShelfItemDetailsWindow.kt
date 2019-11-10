package io.github.rednesto.musicshelf.ui

import io.github.rednesto.musicshelf.MusicShelfBundle
import io.github.rednesto.musicshelf.ShelfItem
import io.github.rednesto.musicshelf.ui.scenes.ShelfItemDetailsController
import io.github.rednesto.musicshelf.utils.loadFxml
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.stage.Stage

object ShelfItemDetailsWindow {
    fun create(item: ShelfItem): Stage = Stage().apply {
        title = MusicShelfBundle.get("shelf_item.details.window_title", item.name)
        scene = Scene(loadFxml<Parent>("/ui/scenes/ShelfItemDetails.fxml", ShelfItemDetailsController(item), MusicShelfBundle.getBundle()))
        scene.accelerators[KeyCodeCombination(KeyCode.ESCAPE)] = Runnable { hide() }
    }
}
