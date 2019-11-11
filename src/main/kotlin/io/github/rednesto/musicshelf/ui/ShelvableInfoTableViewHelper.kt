package io.github.rednesto.musicshelf.ui

import io.github.rednesto.musicshelf.utils.renameToAvoidDuplicates
import javafx.beans.property.ReadOnlyStringWrapper
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.TextFieldTableCell

object ShelvableInfoTableViewHelper {
    fun configure(tableView: TableView<Pair<String, String>>, keyColumn: TableColumn<Pair<String, String>, String>, valueColumn: TableColumn<Pair<String, String>, String>) {
        tableView.selectionModel.selectionMode = SelectionMode.MULTIPLE

        keyColumn.cellFactory = TextFieldTableCell.forTableColumn()
        keyColumn.setCellValueFactory { ReadOnlyStringWrapper(it.value.first) }
        keyColumn.setOnEditCommit { event ->
            val newKey = event.newValue
            if (newKey != event.oldValue) {
                val key = renameToAvoidDuplicates(newKey, tableView.items.map { it.first })
                tableView.items[event.tablePosition.row] = event.rowValue.copy(first = key)
            }
        }
        valueColumn.cellFactory = TextFieldTableCell.forTableColumn()
        valueColumn.setCellValueFactory { ReadOnlyStringWrapper(it.value.second) }
        valueColumn.setOnEditCommit { event ->
            tableView.items[event.tablePosition.row] = event.rowValue.copy(second = event.newValue)
        }
    }
}
