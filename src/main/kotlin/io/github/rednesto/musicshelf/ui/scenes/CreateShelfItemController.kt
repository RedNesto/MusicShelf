package io.github.rednesto.musicshelf.ui.scenes

import io.github.rednesto.musicshelf.*
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
import java.nio.file.Paths
import java.util.*

open class CreateShelfItemController @JvmOverloads constructor(
        val initialName: String? = null,
        val initialFile: Path? = null,
        val initialGroups: Set<String> = emptySet(),
        val initialInfo: Map<String, String> = ShelfItemInfoKeys.DEFAULT_VALUES,
        val lockPath: Boolean = false,
        val shelf: Shelf? = null
) : Initializable {

    var result: ShelfItem? = null
        private set

    @FXML
    lateinit var nameTextField: TextField

    @FXML
    lateinit var filePathTextField: TextField

    @FXML
    lateinit var selectFileButton: Button

    @FXML
    fun selectFileButton_onAction(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
        val chooser = FileChooser().apply {
            title = MusicShelfBundle.get("create.shelf_item.select_file.title")
            if (!filePathTextField.text.isNullOrBlank()) {
                val currentPath = Paths.get(filePathTextField.text).toAbsolutePath()
                if (Files.isDirectory(currentPath)) {
                    initialDirectory = currentPath.toFile()
                } else if (Files.isRegularFile(currentPath)) {
                    initialDirectory = currentPath.parent?.toFile()
                    initialFileName = currentPath.fileName.toString()
                }
            }
        }

        val selectedFile = chooser.showOpenDialog(null) ?: return
        filePathTextField.text = selectedFile.absolutePath
        if (nameTextField.text.isNullOrBlank()) {
            nameTextField.text = selectedFile.nameWithoutExtension
        }
    }

    @FXML
    lateinit var itemInfoLabel: Label

    @FXML
    lateinit var itemInfoTableView: TableView<Pair<String, String>>

    @FXML
    fun itemInfoTableView_onKeyPressed(event: KeyEvent) {
        if (event.code == KeyCode.DELETE) {
            itemInfoTableView.items.removeAll(itemInfoTableView.selectionModel.selectedItems)
        }
    }

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

        if (filePathTextField.text.isNullOrBlank()) {
            Alert(Alert.AlertType.ERROR, MusicShelfBundle.get("create.shelf_item.error.no_path"), ButtonType.OK).apply {
                title = MusicShelfBundle.get("create.shelf_item.error.no_path.title")
                showAndWait()
            }
            return
        }

        val itemPath = Path.of(filePathTextField.text)
        if (Files.notExists(itemPath)) {
            Alert(Alert.AlertType.ERROR, MusicShelfBundle.get("create.shelf_item.error.path_not_exists"), ButtonType.OK).apply {
                title = MusicShelfBundle.get("create.shelf_item.error.path_not_exists.title")
                showAndWait()
            }
            return
        }

        if (!Files.isRegularFile(itemPath)) {
            Alert(Alert.AlertType.ERROR, MusicShelfBundle.get("create.shelf_item.error.path_not_file"), ButtonType.OK).apply {
                title = MusicShelfBundle.get("create.shelf_item.error.path_not_file.title")
                showAndWait()
            }
            return
        }


        val groups = mutableSetOf<String>()
        if (itemGroupsListView.items.isNotEmpty()) {
            groups.addAll(normalizeGroups(itemGroupsListView.items))
        }
        if (addToRootCheckbox.isSelected) {
            groups.add("/")
        }

        result = createItem(itemPath, name, groups, itemInfoTableView.items.toMap())

        filePathTextField.scene.window.hide()
    }

    protected open fun createItem(itemPath: Path, name: String, groups: Set<String>, info: Map<String, String>) =
            ShelfItemFactory.create(itemPath, name, groups, info)

    @FXML
    fun cancelButton_onAction(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
        filePathTextField.scene.window.hide()
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        filePathTextField.textProperty().addListener { _, _, newValue ->
            createButton.isDisable = newValue.isNullOrBlank()
        }

        itemInfoLabel.labelFor = itemInfoTableView
        ShelvableInfoTableViewHelper.configure(itemInfoTableView, infoKeyColumn, infoValueColumn)

        itemInfoTableView.items.addAll(initialInfo.toList())

        itemGroupsLabel.labelFor = itemGroupsListView
        ShelvableGroupsListViewHelper.configure(itemGroupsListView, addToRootCheckbox, shelf)

        if (initialName != null && initialName.isNotBlank()) {
            nameTextField.text = initialName
        }

        if (initialFile != null) {
            val absoluteInitialPath = initialFile.toAbsolutePath()
            filePathTextField.text = absoluteInitialPath.toString()
            if (nameTextField.text.isNullOrBlank()) {
                nameTextField.text = getItemNameForPath(absoluteInitialPath)
            }

            if (lockPath) {
                selectFileButton.isDisable = true
                filePathTextField.isEditable = false
            }
        }

        val sanitizedInitialGroups = initialGroups.toMutableSet()
        addToRootCheckbox.isSelected = sanitizedInitialGroups.removeAll { isRootGroup(it) }
        itemGroupsListView.items.addAll(sanitizedInitialGroups)
        addToRootCheckbox.isDisable = itemGroupsListView.items.isEmpty()
    }
}
