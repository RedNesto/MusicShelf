package io.github.rednesto.musicshelf.ui.scenes

import io.github.rednesto.musicshelf.MusicShelfBundle
import io.github.rednesto.musicshelf.ShelfItem
import io.github.rednesto.musicshelf.ShelfItemFactory
import io.github.rednesto.musicshelf.ShelfItemInfoKeys
import io.github.rednesto.musicshelf.utils.*
import javafx.beans.property.ReadOnlyStringWrapper
import javafx.collections.ListChangeListener
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.control.cell.TextFieldListCell
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.stage.FileChooser
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

open class CreateShelfItemController @JvmOverloads constructor(
        val initialFile: Path? = null,
        val initialGroups: List<String> = emptyList(),
        val initialInfo: Map<String, String> = ShelfItemInfoKeys.DEFAULT_VALUES,
        val lockPath: Boolean = false
) : Initializable {

    var result: ShelfItem? = null
        private set

    @FXML
    lateinit var filePathTextField: TextField

    @FXML
    lateinit var selectFileButton: Button

    @FXML
    fun selectFileButton_onAction(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
        val chooser = FileChooser().apply {
            title = MusicShelfBundle.get("create.shelf_item.select_file.title")
        }

        val selectedFile = chooser.showOpenDialog(null) ?: return
        filePathTextField.text = selectedFile.absolutePath
        updateNameInfo(selectedFile.nameWithoutExtension)
    }

    private fun updateNameInfo(name: String) {
        val defaultNameValue = ShelfItemInfoKeys.DEFAULT_VALUES[ShelfItemInfoKeys.NAME]
                ?: error("There should always be a default item name")
        itemInfoTableView.items.replaceAll { pair ->
            if (pair.first == ShelfItemInfoKeys.NAME && pair.second == defaultNameValue) {
                return@replaceAll Pair(pair.first, name)
            }

            return@replaceAll pair
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
        val key = changeInfoKeyIfNeeded(MusicShelfBundle.get("create.shelf_item.info.default_key"), infoKeys())
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
        val groupName = changeInfoKeyIfNeeded(MusicShelfBundle.get("create.shelf_item.group.default_name"), itemGroupsListView.items)
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

        result = createItem(itemPath, groups.toList(), itemInfoTableView.items.toMap())

        filePathTextField.scene.window.hide()
    }

    protected open fun createItem(itemPath: Path, groups: List<String>, info: Map<String, String>) =
            ShelfItemFactory.create(itemPath, groups, info)

    @FXML
    fun cancelButton_onAction(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
        filePathTextField.scene.window.hide()
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        filePathTextField.textProperty().addListener { _, _, newValue ->
            createButton.isDisable = newValue.isNullOrBlank()
        }

        itemInfoLabel.labelFor = itemInfoTableView
        itemInfoTableView.selectionModel.selectionMode = SelectionMode.MULTIPLE

        infoKeyColumn.cellFactory = TextFieldTableCell.forTableColumn()
        infoKeyColumn.setCellValueFactory { ReadOnlyStringWrapper(it.value.first) }
        infoKeyColumn.setOnEditCommit { event ->
            val newKey = event.newValue
            if (newKey != event.oldValue) {
                val key = changeInfoKeyIfNeeded(newKey, infoKeys())
                itemInfoTableView.items[event.tablePosition.row] = event.rowValue.copy(first = key)
            }
        }
        infoValueColumn.cellFactory = TextFieldTableCell.forTableColumn()
        infoValueColumn.setCellValueFactory { ReadOnlyStringWrapper(it.value.second) }
        infoValueColumn.setOnEditCommit { event ->
            itemInfoTableView.items[event.tablePosition.row] = event.rowValue.copy(second = event.newValue)
        }

        itemInfoTableView.items.addAll(initialInfo.toList())

        itemGroupsLabel.labelFor = itemGroupsListView
        itemGroupsListView.selectionModel.selectionMode = SelectionMode.MULTIPLE
        itemGroupsListView.cellFactory = TextFieldListCell.forListView()
        itemGroupsListView.setOnEditCommit { event ->
            val newGroup = normalizeGroup(event.newValue)
            if (isRootGroup(newGroup)) {
                addToRootCheckbox.isSelected = true
                itemGroupsListView.items.removeAt(event.index)
            } else {
                val oldValue = itemGroupsListView.items[event.index]
                if (newGroup != oldValue) {
                    val newKey = changeInfoKeyIfNeeded(newGroup, itemGroupsListView.items.without(oldValue))
                    itemGroupsListView.items[event.index] = newKey
                }
            }

            event.consume()
        }
        itemGroupsListView.items.addListener(ListChangeListener { addToRootCheckbox.isDisable = itemGroupsListView.items.isEmpty() })

        if (initialFile != null) {
            val absoluteInitialPath = initialFile.toAbsolutePath()
            filePathTextField.text = absoluteInitialPath.toString()
            updateNameInfo(getItemNameForPath(absoluteInitialPath))
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

    private fun changeInfoKeyIfNeeded(originalKey: String, existingKeys: Collection<String>): String {
        if (originalKey !in existingKeys) {
            return originalKey
        }

        fun extractDuplicationMarker(key: String): Int {
            val builder = StringBuilder()
            for (i in key.length - 1 downTo 0) {
                val char = key[i]
                if (!char.isDigit()) {
                    break
                }

                builder.insert(0, char)
            }

            return builder.toString().toInt()
        }

        val duplicationMarkers = existingKeys
                .filter { it.startsWith(originalKey) && it != originalKey }
                .map { existingKey -> extractDuplicationMarker(existingKey) }
                .sorted()

        val newMarker = (0..duplicationMarkers.size).toList().subtract(duplicationMarkers).first()
        return originalKey + newMarker
    }

    private fun infoKeys() = itemInfoTableView.items.map { it.first }
}
