package io.github.rednesto.musicshelf.ui.scenes

import io.github.rednesto.musicshelf.MusicShelf
import io.github.rednesto.musicshelf.MusicShelfBundle
import io.github.rednesto.musicshelf.ShelfItem
import io.github.rednesto.musicshelf.ui.ShelfTreeCell
import io.github.rednesto.musicshelf.utils.DesktopHelper
import io.github.rednesto.musicshelf.utils.loadFxml
import io.github.rednesto.musicshelf.utils.within
import javafx.beans.value.ObservableValue
import javafx.collections.ListChangeListener
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.input.*
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.Window
import javafx.stage.WindowEvent
import java.net.URL
import java.util.*

class MainShelfController : Initializable {

    @FXML
    lateinit var shelfTreeView: TreeView<Any>

    @FXML
    lateinit var emptyShelfPlaceholderHyperlink: Hyperlink

    @FXML
    lateinit var addShelfItemButton: Button

    @FXML
    fun addShelfItemButton_onAction(event: ActionEvent) {
        val dialog = Stage().apply {
            scene = Scene(loadFxml<Parent>("/ui/scenes/CreateShelfItem.fxml", resources = MusicShelfBundle.getBundle()))
            title = MusicShelfBundle.get("create.shelf_item.window_title")
            initModality(Modality.APPLICATION_MODAL)
        }

        dialog.showAndWait()
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
    lateinit var removeShelfItemButton: Button

    @FXML
    fun removeShelfItemButton_onAction(event: ActionEvent) {
        val selectedItemsIds = shelfTreeView.selectionModel.selectedItems.mapNotNull { (it.value as? ShelfItem)?.id }
        selectedItemsIds.forEach { MusicShelf.removeItem(it) }
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        shelfTreeView.selectionModel.selectionMode = SelectionMode.MULTIPLE
        shelfTreeView.setCellFactory { ShelfTreeCell() }
        shelfTreeView.sceneProperty().addListener(::onSceneChange)
        MusicShelf.addChangeListener(shelfChangeListener)
        shelfTreeView.root = TreeItem<Any>().apply {
            children.addListener(ListChangeListener {
                emptyShelfPlaceholderHyperlink.isVisible = children.isEmpty()
            })
            val groups = mutableMapOf<String?, MutableList<TreeItem<Any>>>()
            MusicShelf.getAllItems().forEach { shelfItem ->
                val itemGroups = shelfItem.groups
                if (itemGroups.isEmpty()) {
                    val group = groups.computeIfAbsent(null) { mutableListOf() }
                    group.add(TreeItem(shelfItem))
                } else {
                    itemGroups.forEach {
                        val groupName = if (it == "/") null else it.trimEnd('/')
                        val group = groups.computeIfAbsent(groupName) { mutableListOf() }
                        group.add(TreeItem(shelfItem))
                    }
                }
            }
            val groupNodes = mutableMapOf<String?, TreeItem<Any>>()
            groups.keys.forEach { groupPath ->
                val groupItem = if (groupPath == null) {
                    this
                } else {
                    val groupName = groupPath.substringAfterLast('/')
                    var slashIndex = groupPath.indexOf('/')
                    val parentGroup = if (slashIndex != -1) {
                        val lastGroupPath = groupPath.substring(0, slashIndex)
                        var lastGroupNode: TreeItem<Any> = groupNodes.getOrPut(lastGroupPath) {
                            val item: TreeItem<Any> = TreeItem(lastGroupPath)
                            children.add(item)
                            item
                        }
                        while (true) {
                            val nextSlashIndex = groupPath.indexOf('/', slashIndex + 1)
                            if (nextSlashIndex == -1)
                                break

                            val parentGroupName = groupPath.substring(slashIndex + 1, nextSlashIndex)
                            val nextItem: TreeItem<Any> = groupNodes.getOrPut(groupPath.substring(0, nextSlashIndex)) {
                                val item: TreeItem<Any> = TreeItem(parentGroupName)
                                lastGroupNode.children.add(item)
                                item
                            }


                            lastGroupNode = nextItem
                            slashIndex = nextSlashIndex
                        }

                        lastGroupNode
                    } else null
                    val treeItem = TreeItem<Any>(groupName)
                    if (parentGroup != null) {
                        parentGroup.children.add(treeItem)
                    } else {
                        children.add(treeItem)
                    }

                    treeItem
                }
                groupNodes[groupPath] = groupItem
            }
            groups.forEach { (groupPath, items) ->
                val groupNode = groupNodes[groupPath]
                assert(groupNode != null) { "There should always be a group node" }
                groupNode!!.children.addAll(items)
            }
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
            }
    )

    private val shelfChangeListener = MusicShelfChangeListener()

    private fun onSceneChange(observable: ObservableValue<out Scene>, oldScene: Scene?, newScene: Scene?) {
        if (oldScene != null) {
            oldScene.windowProperty().removeListener(::onWindowChange)
            shortcuts.keys.forEach { oldScene.accelerators.remove(it) }
        }

        if (newScene != null) {
            newScene.windowProperty().addListener(::onWindowChange)
            newScene.accelerators.putAll(shortcuts)
        }
    }

    private fun onWindowChange(observable: ObservableValue<out Window>, oldWindow: Window?, newWindow: Window?) {
        oldWindow?.removeEventHandler(WindowEvent.WINDOW_HIDDEN, ::onWindowClosed)
        newWindow?.addEventHandler(WindowEvent.WINDOW_HIDDEN, ::onWindowClosed)
    }

    private fun onWindowClosed(event: WindowEvent) {
        shelfTreeView.sceneProperty().removeListener(::onSceneChange)
        MusicShelf.removeChangeListener(shelfChangeListener)
    }

    private inner class MusicShelfChangeListener : MusicShelf.ChangeListener {
        override fun onItemAdded(added: ShelfItem) {
            shelfTreeView.root.children.add(TreeItem(added))
        }

        override fun onItemRemoved(removed: ShelfItem) {
            shelfTreeView.root.children.removeAll { it.value == removed }
        }

        override fun onItemReplaced(oldItem: ShelfItem, newItem: ShelfItem) {
            val oldItemIndex = shelfTreeView.root.children.indexOfFirst { it.value == oldItem }
            if (oldItemIndex within shelfTreeView.root.children) {
                shelfTreeView.root.children[oldItemIndex] = TreeItem(newItem)
            } else {
                shelfTreeView.root.children.add(TreeItem(newItem))
            }
        }
    }
}
