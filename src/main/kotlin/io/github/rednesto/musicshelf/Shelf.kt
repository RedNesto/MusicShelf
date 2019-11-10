package io.github.rednesto.musicshelf

import io.github.rednesto.musicshelf.serialization.ProjectStorage
import io.github.rednesto.musicshelf.serialization.ShelfItemStorage
import javafx.collections.FXCollections
import javafx.collections.ObservableSet
import java.nio.file.Path
import java.util.*
import kotlin.collections.HashSet

class Shelf(val name: String, val directory: Path) {

    private val itemsStoragePath = directory.resolve(ShelfItemStorage.DEFAULT_FILE_NAME)
    private val projectsStoragePath = directory.resolve(ProjectStorage.DEFAULT_FILE_NAME)

    private val items: MutableMap<UUID, ShelfItem> = mutableMapOf()
    private val itemsChangeListeners: MutableSet<ChangeListener<ShelfItem>> = mutableSetOf()

    private val projects: MutableMap<UUID, Project> = mutableMapOf()
    private val projectsChangeListeners: MutableSet<ChangeListener<Project>> = mutableSetOf()

    private val allGroupsMutable: ObservableSet<String> = FXCollections.observableSet()
    val allGroups: ObservableSet<String> = FXCollections.unmodifiableObservableSet(allGroupsMutable)

    fun getItem(itemId: UUID): ShelfItem? = items[itemId]

    fun addItem(item: ShelfItem) {
        val previousItem = items.put(item.id, item)
        if (previousItem != null) {
            itemsChangeListeners.forEach { it.onItemReplaced(previousItem, item) }
        } else {
            itemsChangeListeners.forEach { it.onItemAdded(item) }
        }
        allGroupsMutable.addAll(item.groups)
    }

    fun removeItem(itemId: UUID) {
        val removedItem = items.remove(itemId)
        if (removedItem != null) {
            itemsChangeListeners.forEach { it.onItemRemoved(removedItem) }
        }
    }

    fun getAllItems(): Collection<ShelfItem> {
        return items.values
    }

    fun getProject(id: UUID): Project? = projects[id]

    fun addProject(project: Project) {
        val previousProject = projects.put(project.id, project)
        if (previousProject != null) {
            projectsChangeListeners.forEach { it.onItemReplaced(previousProject, project) }
        } else {
            projectsChangeListeners.forEach { it.onItemAdded(project) }
        }
        allGroupsMutable.addAll(project.groups)
    }

    fun removeProject(projectId: UUID) {
        val removedProject = projects.remove(projectId)
        if (removedProject != null) {
            projectsChangeListeners.forEach { it.onItemRemoved(removedProject) }
        }
    }

    fun addItemChangeListener(listener: ChangeListener<ShelfItem>) {
        itemsChangeListeners.add(listener)
    }

    fun removeItemChangeListener(listener: ChangeListener<ShelfItem>) {
        itemsChangeListeners.remove(listener)
    }


    fun getAllShelvables(): Collection<Shelvable> {
        val shelvables = HashSet<Shelvable>(items.size)
        shelvables.addAll(items.values)
        return shelvables
    }


    fun load() {
        clear()
        ShelfItemStorage.load(itemsStoragePath).forEach(::addItem)
        ProjectStorage.load(projectsStoragePath).forEach(::addProject)
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

        val allProjectKeys = HashSet(projects.keys)
        allProjectKeys.forEach(::removeProject)
        check(projects.isEmpty()) { "There should be no projects left" }

        allGroupsMutable.clear()
    }

    interface ChangeListener<T> {
        fun onItemAdded(added: T) = Unit

        fun onItemRemoved(removed: T) = Unit

        fun onItemReplaced(oldItem: T, newItem: T) = Unit
    }

    interface SimpleChangeListener<T> : ChangeListener<T> {
        override fun onItemAdded(added: T) {
            onItemChange(null, added)
        }

        override fun onItemRemoved(removed: T) {
            onItemChange(removed, null)
        }

        override fun onItemReplaced(oldItem: T, newItem: T) {
            onItemChange(oldItem, newItem)
        }

        fun onItemChange(oldItem: T?, newItem: T?) = Unit
    }
}
