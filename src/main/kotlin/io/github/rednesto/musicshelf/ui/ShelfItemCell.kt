package io.github.rednesto.musicshelf.ui

import io.github.rednesto.musicshelf.MusicShelfBundle
import io.github.rednesto.musicshelf.ShelfItem
import io.github.rednesto.musicshelf.nameOrUnnamed
import io.github.rednesto.musicshelf.ui.scenes.ShelfItemDetailsController
import io.github.rednesto.musicshelf.utils.DesktopHelper
import io.github.rednesto.musicshelf.utils.loadFxml
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.ContextMenu
import javafx.scene.control.ListCell
import javafx.scene.text.Text
import javafx.stage.Modality
import javafx.stage.Stage

class ShelfItemCell : ListCell<ShelfItem>() {

    val controller = ShelfItemController()

    val node: Node = loadFxml("/ui/ShelfItemCell.fxml", controller, MusicShelfBundle.getBundle())

    override fun updateItem(item: ShelfItem?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || item == null) {
            text = null
            graphic = null
        } else {
            graphic = node
            controller.update(item)
        }
    }

    inner class ShelfItemController {
        @FXML
        lateinit var name: Text

        @FXML
        lateinit var path: Text

        @FXML
        lateinit var paneContextMenu: ContextMenu

        @FXML
        fun openMenuItem_onAction(event: ActionEvent) {
            DesktopHelper.open(item.path)
        }

        @FXML
        fun showDetailsMenuItem_onAction(event: ActionEvent) {
            Stage().apply {
                title = MusicShelfBundle.get("shelf_item.details.window_title", item.nameOrUnnamed)
                scene = Scene(loadFxml<Parent>("/ui/scenes/ShelfItemDetails.fxml", ShelfItemDetailsController(item), MusicShelfBundle.getBundle()))
                initModality(Modality.APPLICATION_MODAL)
                showAndWait()
            }
        }

        fun update(item: ShelfItem) {
            name.text = item.nameOrUnnamed
            path.text = item.path.toAbsolutePath().toString()
            node.setOnContextMenuRequested { paneContextMenu.show(node, it.screenX, it.screenY) }
        }
    }
}
