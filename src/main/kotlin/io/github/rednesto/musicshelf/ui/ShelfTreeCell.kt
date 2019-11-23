package io.github.rednesto.musicshelf.ui

import io.github.rednesto.musicshelf.MusicShelfBundle
import io.github.rednesto.musicshelf.Project
import io.github.rednesto.musicshelf.Shelf
import io.github.rednesto.musicshelf.ShelfItem
import io.github.rednesto.musicshelf.ui.scenes.CreateShelfItemController
import io.github.rednesto.musicshelf.utils.*
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeItem
import javafx.scene.input.DragEvent
import javafx.scene.input.TransferMode
import javafx.scene.text.Text
import javafx.stage.Modality
import java.io.File
import java.nio.file.Files

class ShelfTreeCell(val shelf: Shelf) : TreeCell<Any>() {

    val shelfItemController = ShelfItemController()
    val shelfItemNode: Node =  loadFxml("/ui/ShelfItemCell.fxml", shelfItemController, MusicShelfBundle.getBundle())

    //val projectController = ShelfItemController()
    //val projectNode: Node =  loadFxml("/ui/ProjectCell.fxml", projectController, MusicShelfBundle.getBundle())

    override fun updateItem(item: Any?, empty: Boolean) {
        super.updateItem(item, empty)

        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        text = null // Somehow Kotlin doesn't like this
        graphic = null
        onContextMenuRequested = null

        if (empty || item == null) {
            removeDragHandlers()
        } else {
            when (item) {
                is String -> {
                    text = item // It is a group
                    addDragHandlers()
                }
                is ShelfItem -> {
                    graphic = shelfItemNode
                    shelfItemController.update(item)
                    removeDragHandlers()
                }
                is Project -> {
                    text = item.name
                    addProjectDragHandlers(item)
                    addProjectContextMenu(item)
                }
                else -> {
                    // TODO log/report this, we should never reach this branch
                    removeDragHandlers()
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
                .forEach { file -> CreateShelfItemDialog.showAndUpdateShelf(shelf, CreateShelfItemController(initialFile = file, initialGroups = groups, shelf = shelf)) }
        event.isDropCompleted = true
        event.consume()
    }

    private fun addProjectDragHandlers(project: Project) {
        setOnDragOver { dragOverHandler(it) }
        setOnDragExited { dragExitedHandler() }
        setOnDragDropped { event ->
            val files = event.dragboard.files ?: return@setOnDragDropped

            val projectFiles = project.files.toMutableMap()
            files.map(File::toPath)
                    .filter { path -> Files.isRegularFile(path) }
                    .associateByTo(projectFiles, ::getItemNameForPath)

            val newProject = project.copy(files = projectFiles)
            shelf.addProject(newProject)

            event.isDropCompleted = true
            event.consume()
        }
    }

    private fun addProjectContextMenu(project: Project) {
        val edit = MenuItem(MusicShelfBundle.get("shelf.project.edit"))
        edit.setOnAction { EditProjectDialog.showAndUpdateShelf(project, shelf) }
        val showDetails = MenuItem(MusicShelfBundle.get("shelf.project.show_details"))
        showDetails.setOnAction {
            with(ProjectDetailsWindow.create(project)) {
                initOwner(this@ShelfTreeCell.scene.window)
                initModality(Modality.WINDOW_MODAL)
                showAndWait()
            }
        }
        val contextMenu = ContextMenu(edit, showDetails)
        setOnContextMenuRequested { contextMenu.show(this.scene.window, it.screenX, it.screenY) }
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
            EditShelfItemDialog.showAndUpdateShelf(item, shelf)
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
            name.text = item.name
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
