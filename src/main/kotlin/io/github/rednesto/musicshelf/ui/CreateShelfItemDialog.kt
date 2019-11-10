package io.github.rednesto.musicshelf.ui

import io.github.rednesto.musicshelf.MusicShelfBundle
import io.github.rednesto.musicshelf.Shelf
import io.github.rednesto.musicshelf.ShelfItem
import io.github.rednesto.musicshelf.ui.scenes.CreateShelfItemController
import io.github.rednesto.musicshelf.utils.configureFxmlLoader
import javafx.scene.Scene
import javafx.stage.Modality
import javafx.stage.Stage

object CreateShelfItemDialog {
    fun showAndGet(controller: CreateShelfItemController? = null): ShelfItem? {
        val loader = configureFxmlLoader("/ui/scenes/CreateShelfItem.fxml", resources = MusicShelfBundle.getBundle())
        if (controller != null) {
            loader.setControllerFactory { controller }
        }
        val stage = Stage().apply {
            scene = Scene(loader.load())
            title = MusicShelfBundle.get("create.shelf_item.window_title")
            initModality(Modality.APPLICATION_MODAL)
        }
        stage.showAndWait()
        return loader.getController<CreateShelfItemController>()?.result
    }

    fun showAndUpdateShelf(shelf: Shelf, controller: CreateShelfItemController? = null) {
        showAndGet(controller)?.let(shelf::addItem)
    }
}
