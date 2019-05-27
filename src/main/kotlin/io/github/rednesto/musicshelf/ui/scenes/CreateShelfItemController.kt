package io.github.rednesto.musicshelf.ui.scenes

import io.github.rednesto.musicshelf.MusicShelf
import io.github.rednesto.musicshelf.MusicShelfBundle
import io.github.rednesto.musicshelf.ShelfItem
import io.github.rednesto.musicshelf.ShelfItemInfoKeys
import javafx.beans.property.ReadOnlyStringWrapper
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.control.cell.TextFieldTableCell
import javafx.stage.FileChooser
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class CreateShelfItemController : Initializable {

    @FXML
    lateinit var filePathTextField: TextField

    @FXML
    fun selectFileButton_onAction(event: ActionEvent) {
        val chooser = FileChooser().apply {
            title = MusicShelfBundle.get("create.shelf_item.select_file.title")
        }

        val selectedFile = chooser.showOpenDialog(null) ?: return
        filePathTextField.text = selectedFile.absolutePath
    }

    @FXML
    lateinit var itemInfoTableView: TableView<Pair<String, String>>

    @FXML
    lateinit var infoKeyColumn: TableColumn<Pair<String, String>, String>

    @FXML
    lateinit var infoValueColumn: TableColumn<Pair<String, String>, String>

    @FXML
    fun addInfoButton_onAction(event: ActionEvent) {
        val key = changeInfoKeyIfNeeded(MusicShelfBundle.get("create.shelf_item.info.default_key"))
        val value = MusicShelfBundle.get("create.shelf_item.info.default_value")
        itemInfoTableView.items.add(key to value)
    }

    @FXML
    fun removeInfoButton_onAction(event: ActionEvent) {
        itemInfoTableView.items.removeAll(itemInfoTableView.selectionModel.selectedItems)
    }

    @FXML
    lateinit var createButton: Button

    @FXML
    fun createButton_onAction(event: ActionEvent) {
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

        MusicShelf.addItem(ShelfItem(UUID.randomUUID(), itemPath, itemInfoTableView.items.toMap(mutableMapOf())))

        filePathTextField.scene.window.hide()
    }

    @FXML
    fun cancelButton_onAction(event: ActionEvent) {
        filePathTextField.scene.window.hide()
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        filePathTextField.textProperty().addListener { observable, oldValue, newValue ->
            createButton.isDisable = newValue.isNullOrBlank()
        }

        itemInfoTableView.selectionModel.selectionMode = SelectionMode.MULTIPLE

        infoKeyColumn.cellFactory = TextFieldTableCell.forTableColumn()
        infoKeyColumn.setCellValueFactory { ReadOnlyStringWrapper(it.value.first) }
        infoKeyColumn.setOnEditCommit { event ->
            val newKey = event.newValue
            if (newKey != event.oldValue) {
                val key = changeInfoKeyIfNeeded(newKey)
                itemInfoTableView.items[event.tablePosition.row] = event.rowValue.copy(first = key)
            }
        }
        infoValueColumn.cellFactory = TextFieldTableCell.forTableColumn()
        infoValueColumn.setCellValueFactory { ReadOnlyStringWrapper(it.value.second) }
        infoValueColumn.setOnEditCommit { event ->
            itemInfoTableView.items[event.tablePosition.row] = event.rowValue.copy(second = event.newValue)
        }

        itemInfoTableView.items.addAll(ShelfItemInfoKeys.DEFAULT_VALUES.toList())
    }

    private fun changeInfoKeyIfNeeded(originalKey: String): String {
        val existingKeys: Collection<String> = itemInfoTableView.items.map { it.first }
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
}
