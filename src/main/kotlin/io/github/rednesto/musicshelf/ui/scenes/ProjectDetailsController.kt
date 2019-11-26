package io.github.rednesto.musicshelf.ui.scenes

import io.github.rednesto.musicshelf.MusicShelfBundle
import io.github.rednesto.musicshelf.Project
import io.github.rednesto.musicshelf.utils.DesktopHelper
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.input.ContextMenuEvent
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseButton
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import java.net.URL
import java.nio.file.Path
import java.util.*

class ProjectDetailsController(val project: Project) : Initializable {

    @FXML
    lateinit var nameText: Text

    @FXML
    lateinit var infoLabel: Label

    @FXML
    lateinit var infoTableView: TableView<Pair<String, String>>

    @FXML
    lateinit var groupsLabel: Label

    @FXML
    lateinit var groupsListView: ListView<String>

    @FXML
    lateinit var filesLabel: Label

    @FXML
    lateinit var filesListView: ListView<Pair<String, Path>>

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        nameText.text = project.name

        infoLabel.labelFor = infoTableView
        infoTableView.items.addAll(project.info.toList())

        groupsLabel.labelFor = groupsListView
        groupsListView.items.addAll(project.groups)

        filesLabel.labelFor = filesListView
        filesListView.setOnKeyPressed { event ->
            if (event.code == KeyCode.ENTER) {
                filesListView.selectionModel.selectedItems.forEach { item ->
                    DesktopHelper.open(item.second)
                }
                event.consume()
            }
        }
        filesListView.setOnMouseClicked { event ->
            if (event.clickCount >= 2 && event.button == MouseButton.PRIMARY) {
                filesListView.selectionModel.selectedItem?.second?.let(DesktopHelper::open)
                event.consume()
            }
        }
        filesListView.selectionModel.selectionMode = SelectionMode.MULTIPLE
        filesListView.setCellFactory { FileListCell() }
        project.filesCollector.collect().toList().let(filesListView.items::addAll)
    }

    private inner class FileListCell : ListCell<Pair<String, Path>>() {

        init {
            val open = MenuItem(MusicShelfBundle.get("project.details.file.open"))
            open.setOnAction { item?.second?.let(DesktopHelper::open) }

            val openAll = MenuItem(MusicShelfBundle.get("project.details.file.open_all"))
            openAll.setOnAction {
                val selectedItems = this@ProjectDetailsController.filesListView.selectionModel.selectedItems
                selectedItems.forEach { DesktopHelper.open(it.second) }
            }
            addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED) {
                val selectedItems = this@ProjectDetailsController.filesListView.selectionModel.selectedItems
                openAll.isVisible = selectedItems.size > 1
            }

            contextMenu = ContextMenu(open, openAll)

            if (DesktopHelper.supportsShow()) {
                val show = MenuItem(MusicShelfBundle.get("project.details.file.show"))
                show.setOnAction { item?.second?.let(DesktopHelper::show) }
                contextMenu.items.add(show)
            }
        }

        override fun updateItem(item: Pair<String, Path>?, empty: Boolean) {
            super.updateItem(item, empty)
            if (item == null || empty) {
                graphic = null
            } else {
                graphic = VBox(Text(item.first), Text(item.second.toAbsolutePath().toString()))
            }
        }
    }
}
