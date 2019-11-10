package io.github.rednesto.musicshelf.ui

import io.github.rednesto.musicshelf.MusicShelfBundle
import io.github.rednesto.musicshelf.Shelf
import io.github.rednesto.musicshelf.ShelfItem
import io.github.rednesto.musicshelf.ui.scenes.EditShelfItemController
import io.github.rednesto.musicshelf.utils.configureFxmlLoader
import javafx.scene.Scene
import javafx.stage.Modality
import javafx.stage.Stage

object EditShelfItemDialog {
    fun showAndGet(shelfItem: ShelfItem, shelf: Shelf): ShelfItem? {
        val loader = configureFxmlLoader("/ui/scenes/CreateShelfItem.fxml", resources = MusicShelfBundle.getBundle())
        loader.setControllerFactory { EditShelfItemController(shelfItem, shelf) }
        val stage = Stage().apply {
            scene = Scene(loader.load())
            title = MusicShelfBundle.get("edit.shelf_item.window_title")
            initModality(Modality.APPLICATION_MODAL)
        }
        stage.showAndWait()
        return loader.getController<EditShelfItemController>()?.result
    }

    fun showAndUpdateShelf(shelfItem: ShelfItem, shelf: Shelf) {
        showAndGet(shelfItem, shelf)?.let(shelf::addItem)
    }
}
