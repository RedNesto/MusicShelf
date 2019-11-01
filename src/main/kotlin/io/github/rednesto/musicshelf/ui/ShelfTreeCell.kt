package io.github.rednesto.musicshelf.ui

import io.github.rednesto.musicshelf.MusicShelfBundle
import io.github.rednesto.musicshelf.ShelfItem
import io.github.rednesto.musicshelf.nameOrUnnamed
import io.github.rednesto.musicshelf.ui.scenes.CreateShelfItemController
import io.github.rednesto.musicshelf.utils.DesktopHelper
import io.github.rednesto.musicshelf.utils.addClass
import io.github.rednesto.musicshelf.utils.loadFxml
import io.github.rednesto.musicshelf.utils.removeClasses
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.ContextMenu
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeItem
import javafx.scene.input.DragEvent
import javafx.scene.input.TransferMode
import javafx.scene.text.Text
import javafx.stage.Modality
import java.io.File
import java.nio.file.Files

class ShelfTreeCell : TreeCell<Any>() {

    val controller = ShelfItemController()

    val node: Node = loadFxml("/ui/ShelfItemCell.fxml", controller, MusicShelfBundle.getBundle())

    override fun updateItem(item: Any?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || item == null) {
            @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
            text = null // Somehow Kotlin doesn't like this
            graphic = null
            removeDragHandlers()
        } else {
            when (item) {
                is String -> {
                    text = item // It is a group
                    addDragHandlers()
                    onContextMenuRequested = null
                }
                is ShelfItem -> {
                    graphic = node
                    controller.update(item)
                    removeDragHandlers()
                }
                else -> {
                    // TODO log/report this, we should never reach this branch
                    removeDragHandlers()
                    onContextMenuRequested = null
                }
            }
        }
    }

    private fun addDragHandlers() {
        setOnDragOver { dragOverHandler(it) }
        setOnDragExited { dragExitedHandler() }
        setOnDragDropped { dragDroppedHandler(it, setOf(treeItem.group(text))) }
    }

    private fun removeDragHandlers() {
        onDragOver = null
        onDragExited = null
        onDragDropped = null
    }

    private fun dragOverHandler(event: DragEvent) {
        if (event.dragboard.hasFiles()) {
            event.acceptTransferModes(TransferMode.LINK)
            this@ShelfTreeCell.addClass("drag-over-highlight-cell")
            treeView?.removeClasses("drag-over-highlight")
        }
        event.consume()
    }

    private fun dragExitedHandler() {
        // We do not add the treeView highlight back here because aborting the drag in some way (like pressing escape on Windows)
        // will not make it to the dragExit event of the treeView, thus leaving the highlight until another drag event comes.
        this@ShelfTreeCell.styleClass.removeAll("drag-over-highlight-cell")
    }

    private fun dragDroppedHandler(event: DragEvent, groups: Set<String>) {
        val files = event.dragboard.files ?: return
        files.map(File::toPath)
                .filter { path -> Files.isRegularFile(path) }
                .forEach { file -> CreateShelfItemDialog.showAndUpdateShelf(CreateShelfItemController(file, groups)) }
        event.isDropCompleted = true
        event.consume()
    }

    inner class ShelfItemController {
        lateinit var item: ShelfItem

        @FXML
        lateinit var name: Text

        @FXML
        lateinit var path: Text

        @FXML
        lateinit var paneContextMenu: ContextMenu

        @FXML
        fun openMenuItem_onAction(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
            DesktopHelper.open(item.path)
        }

        @FXML
        fun showDetailsMenuItem_onAction(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
            with(ShelfItemDetailsWindow.create(item)) {
                initModality(Modality.APPLICATION_MODAL)
                showAndWait()
            }
        }

        @FXML
        fun editMenuItem_onAction(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
            EditShelfItemDialog.showAndUpdateShelf(item)
        }

        @FXML
        fun onDragOver(event: DragEvent) {
            dragOverHandler(event)
        }

        @FXML
        fun onDragExited(@Suppress("UNUSED_PARAMETER") event: DragEvent) {
            dragExitedHandler()
        }

        @FXML
        fun onDragDropped(event: DragEvent) {
            dragDroppedHandler(event, setOf(this@ShelfTreeCell.treeItem.group()))
        }

        fun update(item: ShelfItem) {
            this.item = item
            name.text = item.nameOrUnnamed
            path.text = item.path.toAbsolutePath().toString()
            path.style = if (Files.notExists(item.path)) "-fx-fill: red" else null
            this@ShelfTreeCell.setOnContextMenuRequested { paneContextMenu.show(this@ShelfTreeCell, it.screenX, it.screenY) }
        }
    }
}

private fun TreeItem<Any>.group(lastGroup: String? = null): String {
    if (parent?.value == null) {
        return lastGroup ?: "/"
    }

    var nextParent = parent
    val groups = mutableListOf<String>()
    while (nextParent != null) {
        val parentValue = nextParent.value
        if (parentValue != null) {
            val segment = parentValue as? String ?: continue
            groups.add(0, segment)
        }
        nextParent = nextParent.parent
    }

    if (lastGroup != null) {
        groups.add(lastGroup)
    }

    return groups.joinToString("/")
}
