package io.github.rednesto.musicshelf.ui

import io.github.rednesto.musicshelf.ShelfItemInfoIndex
import io.github.rednesto.musicshelf.utils.renameToAvoidDuplicates
import javafx.beans.property.ReadOnlyStringWrapper
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.ComboBoxTableCell

object ShelvableInfoTableViewHelper {
    fun configure(tableView: TableView<Pair<String, String>>, keyColumn: TableColumn<Pair<String, String>, String>, valueColumn: TableColumn<Pair<String, String>, String>) {
        tableView.selectionModel.selectionMode = SelectionMode.MULTIPLE

        keyColumn.setCellFactory {
            ComboBoxTableCell<Pair<String, String>, String>(FXCollections.observableArrayList(ShelfItemInfoIndex.getAllKeys())).apply {
                isComboBoxEditable = true
            }
        }
        keyColumn.setCellValueFactory { ReadOnlyStringWrapper(it.value.first) }
        keyColumn.setOnEditCommit { event ->
            val newKey = event.newValue
            if (newKey != event.oldValue) {
                val key = renameToAvoidDuplicates(newKey, tableView.items.map { it.first })
                tableView.items[event.tablePosition.row] = event.rowValue.copy(first = key)
            }
        }
        valueColumn.setCellFactory {
            InfoValueComboBoxTableCell()
        }
        valueColumn.setCellValueFactory { ReadOnlyStringWrapper(it.value.second) }
        valueColumn.setOnEditCommit { event ->
            tableView.items[event.tablePosition.row] = event.rowValue.copy(second = event.newValue)
        }
    }
}

private class InfoValueComboBoxTableCell : ComboBoxTableCell<Pair<String, String>, String>() {
    private fun reloadItemsFromIndex(infoKey: String) {
        items.clear()
        items.addAll(ShelfItemInfoIndex[infoKey])
    }

    private val rowValueChangeListener = ChangeListener<Pair<String, String>> { _, oldValue, newValue ->
        val infoPair = oldValue ?: newValue
        reloadItemsFromIndex(infoPair.first)
    }

    init {
        isComboBoxEditable = true
        tableRowProperty().addListener { _, oldValue, newValue ->
            if (oldValue == null && newValue != null) {
                // This is probably the first time this cell is placed in the table.
                // We fill this combobox items here because they are not until
                // the first value change of this row
                newValue.item?.first?.let(::reloadItemsFromIndex)
            }
            oldValue?.itemProperty()?.removeListener(rowValueChangeListener)
            newValue?.itemProperty()?.addListener(rowValueChangeListener)
        }
    }
}
