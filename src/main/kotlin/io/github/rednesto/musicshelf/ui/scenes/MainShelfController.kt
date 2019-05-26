package io.github.rednesto.musicshelf.ui.scenes

import io.github.rednesto.musicshelf.MusicShelf
import io.github.rednesto.musicshelf.ShelfItem
import io.github.rednesto.musicshelf.ui.ShelfItemCell
import io.github.rednesto.musicshelf.utils.within
import javafx.beans.value.ObservableValue
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.ListView
import javafx.scene.control.SelectionMode
import javafx.stage.Window
import javafx.stage.WindowEvent
import java.net.URL
import java.util.*

class MainShelfController : Initializable {

    @FXML
    lateinit var shelfListView: ListView<ShelfItem>

    @FXML
    lateinit var addShelfItemButton: Button

    @FXML
    fun addShelfItemButton_onAction(event: ActionEvent) {
        // TODO
    }

    @FXML
    lateinit var removeShelfItemButton: Button

    @FXML
    fun removeShelfItemButton_onAction(event: ActionEvent) {
        val selectedItemsIds = shelfListView.selectionModel.selectedItems.map { it.id }
        selectedItemsIds.forEach { MusicShelf.removeItem(it) }
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        shelfListView.selectionModel.selectionMode = SelectionMode.MULTIPLE
        shelfListView.setCellFactory { ShelfItemCell() }
        shelfListView.sceneProperty().addListener(::onSceneChange)
        MusicShelf.addChangeListener(shelfChangeListener)
        shelfListView.items.addAll(MusicShelf.getAllItems())
    }

    private val shelfChangeListener = MusicShelfChangeListener()

    private fun onSceneChange(observable: ObservableValue<out Scene>, oldScene: Scene?, newScene: Scene?) {
        oldScene?.windowProperty()?.removeListener(::onWindowChange)
        newScene?.windowProperty()?.addListener(::onWindowChange)
    }

    private fun onWindowChange(observable: ObservableValue<out Window>, oldWindow: Window?, newWindow: Window?) {
        oldWindow?.removeEventHandler(WindowEvent.WINDOW_HIDDEN, ::onWindowClosed)
        newWindow?.addEventHandler(WindowEvent.WINDOW_HIDDEN, ::onWindowClosed)
    }

    private fun onWindowClosed(event: WindowEvent) {
        shelfListView.sceneProperty().removeListener(::onSceneChange)
        MusicShelf.removeChangeListener(shelfChangeListener)
    }

    private inner class MusicShelfChangeListener : MusicShelf.ChangeListener {
        override fun onItemAdded(added: ShelfItem) {
            shelfListView.items.add(added)
        }

        override fun onItemRemoved(removed: ShelfItem) {
            shelfListView.items.remove(removed)
        }

        override fun onItemReplaced(oldItem: ShelfItem, newItem: ShelfItem) {
            val oldItemIndex = shelfListView.items.indexOf(oldItem)
            if (oldItemIndex within shelfListView.items) {
                shelfListView.items[oldItemIndex] = newItem
            } else {
                shelfListView.items.add(newItem)
            }
        }
    }
}
