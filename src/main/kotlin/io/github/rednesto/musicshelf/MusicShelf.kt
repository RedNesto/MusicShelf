package io.github.rednesto.musicshelf

import io.github.rednesto.musicshelf.serialization.ShelfItemStorage
import java.nio.file.Path
import java.util.*

object MusicShelf {

    private val items: MutableMap<UUID, ShelfItem> = mutableMapOf()
    private val changeListeners: MutableSet<ChangeListener> = mutableSetOf()

    fun getItem(itemId: UUID): ShelfItem? = items[itemId]

    fun addItem(item: ShelfItem) {
        val previousItem = items.put(item.id, item)
        if (previousItem != null) {
            changeListeners.forEach { it.onItemReplaced(previousItem, item) }
        } else {
            changeListeners.forEach { it.onItemAdded(item) }
        }
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

    fun load(filePath: Path = ShelfItemStorage.DEFAULT_FILE_PATH) {
        val loadedItems = ShelfItemStorage.load(filePath).associateBy(ShelfItem::id)
        items.clear()
        items.putAll(loadedItems)
    }

    fun save(filePath: Path = ShelfItemStorage.DEFAULT_FILE_PATH) {
        ShelfItemStorage.save(items.values.toList(), filePath)
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
