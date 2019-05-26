package io.github.rednesto.musicshelf.ui

import io.github.rednesto.musicshelf.ShelfItem
import io.github.rednesto.musicshelf.name
import io.github.rednesto.musicshelf.utils.loadFxml
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.ListCell
import javafx.scene.text.Text

class ShelfItemCell : ListCell<ShelfItem>() {

    val controller = ShelfItemController()

    val node: Node = loadFxml("/ui/ShelfItemCell.fxml", controller)

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

        fun update(item: ShelfItem) {
            name.text = item.name ?: "<Unnamed>"
            path.text = item.path.toAbsolutePath().toString()
        }
    }
}
