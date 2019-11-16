package io.github.rednesto.musicshelf.ui.scenes;

import io.github.rednesto.musicshelf.MusicShelfBundle
import io.github.rednesto.musicshelf.Project
import io.github.rednesto.musicshelf.Shelf
import io.github.rednesto.musicshelf.ShelfItemInfoKeys
import io.github.rednesto.musicshelf.ui.ProjectFilesTableViewHelper
import io.github.rednesto.musicshelf.ui.ShelvableGroupsListViewHelper
import io.github.rednesto.musicshelf.ui.ShelvableInfoTableViewHelper
import io.github.rednesto.musicshelf.utils.getItemNameForPath
import io.github.rednesto.musicshelf.utils.isRootGroup
import io.github.rednesto.musicshelf.utils.normalizeGroups
import io.github.rednesto.musicshelf.utils.renameToAvoidDuplicates
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.stage.FileChooser
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

open class CreateProjectController @JvmOverloads constructor(
        val initialName: String? = null,
        val initialGroups: Set<String> = emptySet(),
        val initialInfo: Map<String, String> = ShelfItemInfoKeys.DEFAULT_VALUES,
        val initialFiles: Map<String, Path> = emptyMap(),
        val shelf: Shelf? = null
) : Initializable {

    var result: Project? = null
        private set

    @FXML
    lateinit var nameTextField: TextField

    @FXML
    lateinit var filesLabel: Label

    @FXML
    lateinit var filesTableView: TableView<Pair<String, Path>>

    @FXML
    lateinit var filesNameColumn: TableColumn<Pair<String, Path>, String>

    @FXML
    lateinit var filesPathColumn: TableColumn<Pair<String, Path>, Path>

    @FXML
    fun addFileButton_onAction(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
        val fileChooser = FileChooser().apply {
            title = MusicShelfBundle.get("create.project.file.file_choose_title")
        }
        val selectedFiles = fileChooser.showOpenMultipleDialog(filesTableView.scene.window)
                ?: return
        selectedFiles.mapNotNullTo(filesTableView.items) { file ->
            val path = file.toPath()
            val name = renameToAvoidDuplicates(getItemNameForPath(path), filesTableView.items.map { it.first })
            name to path
        }
    }

    @FXML
    fun removeFileButton_onAction(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
        filesTableView.items.removeAll(filesTableView.selectionModel.selectedItems)
    }

    @FXML
    fun filesTableView_onKeyPressed(event: KeyEvent) {
        if (event.code == KeyCode.DELETE) {
            filesTableView.items.removeAll(filesTableView.selectionModel.selectedItems)
        }
    }

    @FXML
    lateinit var itemInfoTableView: TableView<Pair<String, String>>

    @FXML
    fun itemInfoTableView_onKeyPressed(event: KeyEvent) {
        if (event.code == KeyCode.DELETE) {
            itemInfoTableView.items.removeAll(itemInfoTableView.selectionModel.selectedItems)
        }
    }

    @FXML
    lateinit var itemInfoLabel: Label

    @FXML
    lateinit var infoKeyColumn: TableColumn<Pair<String, String>, String>

    @FXML
    lateinit var infoValueColumn: TableColumn<Pair<String, String>, String>

    @FXML
    fun addInfoButton_onAction(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
        val key = renameToAvoidDuplicates(MusicShelfBundle.get("create.shelf_item.info.default_key"), itemInfoTableView.items.map { it.first })
        val value = MusicShelfBundle.get("create.shelf_item.info.default_value")
        itemInfoTableView.items.add(key to value)
    }

    @FXML
    fun removeInfoButton_onAction(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
        itemInfoTableView.items.removeAll(itemInfoTableView.selectionModel.selectedItems)
    }

    @FXML
    lateinit var itemGroupsLabel: Label

    @FXML
    lateinit var itemGroupsListView: ListView<String>

    @FXML
    fun itemGroupsListView_onKeyPressed(event: KeyEvent) {
        if (event.code == KeyCode.DELETE) {
            itemGroupsListView.items.removeAll(itemGroupsListView.selectionModel.selectedItems)
        }
    }

    @FXML
    lateinit var addToRootCheckbox: CheckBox

    @FXML
    fun addGroupButton_onAction(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
        val groupName = renameToAvoidDuplicates(MusicShelfBundle.get("create.shelf_item.group.default_name"), itemGroupsListView.items)
        itemGroupsListView.items.add(groupName)
        itemGroupsListView.scrollTo(groupName)
        itemGroupsListView.requestFocus()
        itemGroupsListView.selectionModel.clearAndSelect(itemGroupsListView.items.lastIndex)
    }

    @FXML
    fun removeGroupButton_onAction(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
        itemGroupsListView.items.removeAll(itemGroupsListView.selectionModel.selectedItems)
    }

    @FXML
    lateinit var createButton: Button

    @FXML
    fun createButton_onAction(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
        val name = nameTextField.text
        if (name.isNullOrBlank()) {
            Alert(Alert.AlertType.ERROR, MusicShelfBundle.get("create.shelf_item.error.empty_name"), ButtonType.OK).apply {
                title = MusicShelfBundle.get("create.shelf_item.error.empty_name.title")
                showAndWait()
            }
            return
        }

        val files = filesTableView.items.mapNotNull { pair ->
            if (!Files.isRegularFile(pair.second)) null else pair
        }.toMap()

        val groups = mutableSetOf<String>()
        if (itemGroupsListView.items.isNotEmpty()) {
            groups.addAll(normalizeGroups(itemGroupsListView.items))
        }
        if (addToRootCheckbox.isSelected) {
            groups.add("/")
        }

        result = createItem(name, groups, itemInfoTableView.items.toMap(), files)

        nameTextField.scene.window.hide()
    }

    protected open fun createItem(name: String, groups: Set<String>, info: Map<String, String>, files: Map<String, Path>) =
            Project(UUID.randomUUID(), name, groups, info, files)

    @FXML
    fun cancelButton_onAction(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
        nameTextField.scene.window.hide()
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        ProjectFilesTableViewHelper.configure(filesTableView, filesNameColumn, filesPathColumn)
        filesLabel.labelFor = filesTableView
        filesTableView.items.addAll(initialFiles.toList())

        itemInfoLabel.labelFor = itemInfoTableView
        ShelvableInfoTableViewHelper.configure(itemInfoTableView, infoKeyColumn, infoValueColumn)

        itemInfoTableView.items.addAll(initialInfo.toList())

        itemGroupsLabel.labelFor = itemGroupsListView
        ShelvableGroupsListViewHelper.configure(itemGroupsListView, addToRootCheckbox, shelf)

        if (initialName != null && initialName.isNotBlank()) {
            nameTextField.text = initialName
        }

        val sanitizedInitialGroups = initialGroups.toMutableSet()
        addToRootCheckbox.isSelected = sanitizedInitialGroups.removeAll { isRootGroup(it) }
        itemGroupsListView.items.addAll(sanitizedInitialGroups)
        addToRootCheckbox.isDisable = itemGroupsListView.items.isEmpty()
    }
}
