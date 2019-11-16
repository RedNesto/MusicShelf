package io.github.rednesto.musicshelf.ui

import io.github.rednesto.musicshelf.MusicShelfBundle
import io.github.rednesto.musicshelf.utils.renameToAvoidDuplicates
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.property.ReadOnlyStringWrapper
import javafx.scene.control.*
import javafx.scene.control.cell.TextFieldTableCell
import javafx.util.StringConverter
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.nio.file.Paths

object ProjectFilesTableViewHelper {
    fun configure(
            tableView: TableView<Pair<String, Path>>,
            nameColumn: TableColumn<Pair<String, Path>, String>,
            pathColumn: TableColumn<Pair<String, Path>, Path>) {
        tableView.selectionModel.selectionMode = SelectionMode.MULTIPLE

        nameColumn.cellFactory = TextFieldTableCell.forTableColumn()
        nameColumn.setCellValueFactory { ReadOnlyStringWrapper(it.value.first) }
        nameColumn.setOnEditCommit { event ->
            val newKey = event.newValue
            if (newKey != event.oldValue) {
                val key = renameToAvoidDuplicates(newKey, tableView.items.map { it.first })
                tableView.items[event.tablePosition.row] = event.rowValue.copy(first = key)
            }
        }
        pathColumn.cellFactory = TextFieldTableCell.forTableColumn(PathStringConverter)
        pathColumn.setCellValueFactory { ReadOnlyObjectWrapper(it.value.second) }
        pathColumn.setOnEditCommit { event ->
            tableView.items[event.tablePosition.row] = event.rowValue.copy(second = event.newValue)
        }
    }
}

private object PathStringConverter : StringConverter<Path>() {
    override fun toString(`object`: Path): String = `object`.toAbsolutePath().normalize().toString()

    override fun fromString(string: String): Path = try {
        Paths.get(string).toAbsolutePath()
    } catch (e: InvalidPathException) {
        Alert(Alert.AlertType.ERROR, "Invalid path: $string", ButtonType.OK).apply {
            title = MusicShelfBundle.get("create.project.error.invalid_file_path")
        }.showAndWait()
        Paths.get("")
    }
}
