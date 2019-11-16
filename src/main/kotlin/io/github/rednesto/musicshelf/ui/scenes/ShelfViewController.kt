package io.github.rednesto.musicshelf.ui.scenes

import io.github.rednesto.musicshelf.Project
import io.github.rednesto.musicshelf.Shelf
import io.github.rednesto.musicshelf.ShelfItem
import io.github.rednesto.musicshelf.ui.CreateProjectDialog
import io.github.rednesto.musicshelf.ui.CreateShelfItemDialog
import io.github.rednesto.musicshelf.ui.ShelfTreeCell
import io.github.rednesto.musicshelf.ui.ShelfTreeViewHelper
import io.github.rednesto.musicshelf.utils.DesktopHelper
import io.github.rednesto.musicshelf.utils.addClass
import io.github.rednesto.musicshelf.utils.removeClasses
import javafx.beans.value.ObservableValue
import javafx.collections.ListChangeListener
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.input.*
import javafx.stage.Window
import javafx.stage.WindowEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.javafx.JavaFx
import java.io.File
import java.lang.Runnable
import java.net.URL
import java.nio.file.Files
import java.util.*

class ShelfViewController(val shelf: Shelf) : Initializable {

    @FXML
    lateinit var shelfSearchTextField: TextField

    private var filterUpdateJob: Job? = null

    @FXML
    fun shelfSearchTextField_onKeyTyped(@Suppress("UNUSED_PARAMETER") event: KeyEvent) {
        filterUpdateJob?.cancel()
        filterUpdateJob = GlobalScope.launch(Dispatchers.JavaFx) {
            delay(1000)
            shelfTreeViewHelper.filter(shelfSearchTextField.text)
            filterUpdateJob = null
        }
    }

    @FXML
    lateinit var shelfTreeView: TreeView<Any>
    private lateinit var shelfTreeViewHelper: ShelfTreeViewHelper

    @FXML
    fun shelfTreeView_onDragOver(event: DragEvent) {
        if (event.dragboard.hasFiles()) {
            event.acceptTransferModes(TransferMode.LINK)
            shelfTreeView.addClass("drag-over-highlight")
        }
        event.consume()
    }

    @FXML
    fun shelfTreeView_onDragExited(@Suppress("UNUSED_PARAMETER") event: DragEvent) {
        shelfTreeView.removeClasses("drag-over-highlight")
    }

    @FXML
    fun shelfTreeView_onDragDropped(event: DragEvent) {
        val files = event.dragboard.files ?: return
        files.map(File::toPath)
                .filter { path -> Files.isRegularFile(path) }
                .forEach { file -> CreateShelfItemDialog.showAndUpdateShelf(shelf, CreateShelfItemController(initialFile = file, shelf = shelf)) }
        event.isDropCompleted = true
        event.consume()
    }

    @FXML
    lateinit var emptyShelfPlaceholderHyperlink: Hyperlink

    @FXML
    lateinit var addShelfItemButton: SplitMenuButton

    @FXML
    fun addShelfItemButton_onAction(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
        CreateShelfItemDialog.showAndUpdateShelf(shelf)
    }

    @FXML
    fun shelfTreeView_onMouseClicked(event: MouseEvent) {
        if (event.button == MouseButton.PRIMARY && event.clickCount >= 2) {
            val selectedItem = shelfTreeView.selectionModel.selectedItem?.value
            if (selectedItem is ShelfItem) {
                DesktopHelper.open(selectedItem.path)
            }
        }
    }

    @FXML
    fun addProjectMenuItem_onAction(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
        CreateProjectDialog.showAndUpdateShelf(shelf)
    }

    @FXML
    lateinit var removeShelfItemButton: Button

    @FXML
    fun removeShelfItemButton_onAction(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
        shelfTreeView.selectionModel.selectedItems.forEach { treeItem ->
            val value = treeItem.value ?: return@forEach
            when (value) {
                is ShelfItem -> shelf.removeItem(value.id)
                is Project -> shelf.removeProject(value.id)
            }
        }
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        shelfTreeViewHelper = ShelfTreeViewHelper(shelfTreeView, shelf)
        shelfTreeView.selectionModel.selectionMode = SelectionMode.MULTIPLE
        shelfTreeView.padding = Insets(1.0)
        shelfTreeView.setCellFactory { ShelfTreeCell(shelf) }
        shelfTreeView.sceneProperty().addListener(::onSceneChange)
        shelf.addItemChangeListener(shelfChangeListener)
        shelf.addProjectChangeListener(projectChangeListener)
        shelfTreeViewHelper.recreateRootNode().apply {
            emptyShelfPlaceholderHyperlink.isVisible = children.isEmpty()
            children.addListener(ListChangeListener { emptyShelfPlaceholderHyperlink.isVisible = children.isEmpty() })
        }
    }

    private val shortcuts: Map<KeyCombination, Runnable> = mapOf(
            KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_ANY) to Runnable {
                fun openItem(item: TreeItem<*>) {
                    item.children.forEach { childItem ->
                        when (val value = childItem.value) {
                            is ShelfItem -> DesktopHelper.open(value.path)
                            is String -> openItem(childItem)
                        }
                    }
                }
                shelfTreeView.selectionModel.selectedItems.forEach(::openItem)
            },
            KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_ANY) to Runnable { shelfSearchTextField.requestFocus() }
    )

    private val shelfChangeListener = ShelfItemChangeListener()
    private val projectChangeListener = ProjectChangeListener()

    private fun onSceneChange(@Suppress("UNUSED_PARAMETER") observable: ObservableValue<out Scene>, oldScene: Scene?, newScene: Scene?) {
        if (oldScene != null) {
            oldScene.windowProperty().removeListener(::onWindowChange)
            shortcuts.keys.forEach { oldScene.accelerators.remove(it) }
        }

        if (newScene != null) {
            newScene.windowProperty().addListener(::onWindowChange)
            newScene.accelerators.putAll(shortcuts)
        }
    }

    private fun onWindowChange(@Suppress("UNUSED_PARAMETER") observable: ObservableValue<out Window>, oldWindow: Window?, newWindow: Window?) {
        oldWindow?.removeEventHandler(WindowEvent.WINDOW_HIDDEN, ::onWindowClosed)
        newWindow?.addEventHandler(WindowEvent.WINDOW_HIDDEN, ::onWindowClosed)
    }

    private fun onWindowClosed(@Suppress("UNUSED_PARAMETER") event: WindowEvent) {
        shelfTreeView.sceneProperty().removeListener(::onSceneChange)
        shelf.removeItemChangeListener(shelfChangeListener)
        shelf.removeProjectChangeListener(projectChangeListener)
    }

    private inner class ShelfItemChangeListener : Shelf.SimpleChangeListener<ShelfItem> {
        override fun onItemChange(oldItem: ShelfItem?, newItem: ShelfItem?) {
            if (oldItem != null) {
                shelfTreeViewHelper.removeShelvable(oldItem)
            }

            if (newItem != null) {
                shelfTreeViewHelper.addShelvable(newItem)
            }
        }
    }

    private inner class ProjectChangeListener : Shelf.SimpleChangeListener<Project> {
        override fun onItemChange(oldItem: Project?, newItem: Project?) {
            if (oldItem != null) {
                shelfTreeViewHelper.removeShelvable(oldItem)
            }

            if (newItem != null) {
                shelfTreeViewHelper.addShelvable(newItem)
            }
        }
    }
}
