package io.github.rednesto.musicshelf.ui

import io.github.rednesto.musicshelf.Shelf
import io.github.rednesto.musicshelf.utils.*
import javafx.collections.*
import javafx.scene.control.CheckBox
import javafx.scene.control.ListView
import javafx.scene.control.SelectionMode
import javafx.scene.control.cell.ComboBoxListCell

object ShelvableGroupsListViewHelper {
    fun configure(listView: ListView<String>, addToRootCheckbox: CheckBox, shelf: Shelf?) {
        val availableGroups: ObservableList<String> = FXCollections.observableArrayList()

        listView.selectionModel.selectionMode = SelectionMode.MULTIPLE
        listView.setCellFactory { ComboBoxListCell(availableGroups).apply { isComboBoxEditable = true } }
        listView.setOnEditCommit { event ->
            val newGroup = normalizeGroup(event.newValue)
            if (isRootGroup(newGroup)) {
                addToRootCheckbox.isSelected = true
                listView.items.removeAt(event.index)
            } else {
                val oldValue = listView.items[event.index]
                if (newGroup != oldValue) {
                    val newKey = renameToAvoidDuplicates(newGroup, listView.items.without(oldValue))
                    listView.items[event.index] = newKey
                }
            }

            event.consume()
        }
        listView.items.addListener(ListChangeListener { addToRootCheckbox.isDisable = listView.items.isEmpty() })
        listView.items.addListener(ListChangeListener { change ->
            while (change.next()) {
                change.addedSubList.forEach { availableGroups.remove(it) }
                change.removed.forEach { availableGroups.addIfAbsent(it) }
            }
            availableGroups.sort()
        })

        if (shelf != null) {
            shelf.allGroups.addListener(WeakSetChangeListener(SetChangeListener { change ->
                val added = change.elementAdded
                if (added != null && !listView.items.contains(added) && !isRootGroup(added)) {
                    availableGroups.addIfAbsent(added)
                }
                availableGroups.sort()
            }))
            availableGroups.addAll(shelf.allGroups)
            availableGroups.removeAll(listView.items)
            availableGroups.remove("/")
            availableGroups.sort()
        }
    }
}
