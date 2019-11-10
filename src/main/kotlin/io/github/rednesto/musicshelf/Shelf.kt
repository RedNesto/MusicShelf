package io.github.rednesto.musicshelf

import io.github.rednesto.musicshelf.serialization.ShelfItemStorage
import javafx.collections.FXCollections
import javafx.collections.ObservableSet
import java.nio.file.Path
import java.util.*
import kotlin.collections.HashSet

class Shelf(val name: String, val directory: Path) {

    private val itemsStoragePath = directory.resolve(ShelfItemStorage.DEFAULT_FILE_NAME)

    private val items: MutableMap<UUID, ShelfItem> = mutableMapOf()
    private val changeListeners: MutableSet<ChangeListener> = mutableSetOf()
    private val allGroupsMutable: ObservableSet<String> = FXCollections.observableSet()
    val allGroups: ObservableSet<String> = FXCollections.unmodifiableObservableSet(allGroupsMutable)

    fun getItem(itemId: UUID): ShelfItem? = items[itemId]

    fun addItem(item: ShelfItem) {
        val previousItem = items.put(item.id, item)
        if (previousItem != null) {
            changeListeners.forEach { it.onItemReplaced(previousItem, item) }
        } else {
            changeListeners.forEach { it.onItemAdded(item) }
        }
        allGroupsMutable.addAll(item.groups)
    }

    fun removeItem(itemId: UUID) {
        val removedItem = items.remove(itemId)
        if (removedItem != null) {
            changeListeners.forEach { it.onItemRemoved(removedItem) }
        }
    }

    fun getAllItems(): Collection<ShelfItem> {
        return items.values
    }

    fun addChangeListener(listener: ChangeListener) {
        changeListeners.add(listener)
    }

    fun removeChangeListener(listener: ChangeListener) {
        changeListeners.remove(listener)
    }

    fun load() {
        clear()
        val loadedItems = ShelfItemStorage.load(itemsStoragePath).associateBy(ShelfItem::id)
        loadedItems.values.forEach(::addItem)
    }

    fun save() {
        ShelfItemStorage.save(items.values.toList(), itemsStoragePath)
    }

    fun forgetUnusedGroups() {
        val existingGroups = mutableSetOf<String>()
        items.values.forEach { existingGroups.addAll(it.groups) }
        allGroupsMutable.removeIf { !existingGroups.contains(it) }
    }

    private fun clear() {
        val allKeys = HashSet(items.keys)
        allKeys.forEach(::removeItem)
        check(items.isEmpty()) { "There should be no items left" }

        allGroupsMutable.clear()
    }

    interface ChangeListener {
        fun onItemAdded(added: ShelfItem) = Unit

        fun onItemRemoved(removed: ShelfItem) = Unit

        fun onItemReplaced(oldItem: ShelfItem, newItem: ShelfItem) = Unit
    }

    interface SimpleChangeListener : ChangeListener {
        override fun onItemAdded(added: ShelfItem) {
            onItemChange(null, added)
        }

        override fun onItemRemoved(removed: ShelfItem) {
            onItemChange(removed, null)
        }

        override fun onItemReplaced(oldItem: ShelfItem, newItem: ShelfItem) {
            onItemChange(oldItem, newItem)
        }

        fun onItemChange(oldItem: ShelfItem?, newItem: ShelfItem?) = Unit
    }
}
