package io.github.rednesto.musicshelf.ui

import io.github.rednesto.musicshelf.MusicShelf
import io.github.rednesto.musicshelf.ShelfItem
import io.github.rednesto.musicshelf.utils.normalizeGroup
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView

class ShelfTreeViewHelper(val treeView: TreeView<Any>) {

    private val groupItems = mutableMapOf<String, TreeItem<Any>>()
    private val itemsByGroup = mutableMapOf<String, MutableList<TreeItem<Any>>>()

    fun recreateRootNode(): TreeItem<Any> {
        val rootNode = TreeItem<Any>()

        // Group all items of the shelf
        MusicShelf.getAllItems().forEach { addToGroups(it) }

        /*
         * Create a TreeItem for every full group.
         * Example considering the following items:
         * - groupA/item1
         * - groupA/groupB/item2
         * - groupC/item3
         * - groupD/groupE/item4
         * We will create 5 TreeItems:
         * - groupA
         * - groupA/groupB
         * - groupC
         * - groupD
         * - groupD/groupE
         */
        itemsByGroup.keys.forEach { group -> getGroupItem(group, rootNode) }

        // Create TreeItems for each ShelfItems and move them to their appropriate group TreeItem
        itemsByGroup.forEach { (fullGroup, item) ->
            val groupItem = groupItems[fullGroup] ?: rootNode
            groupItem.children.addAll(item)
        }

        treeView.root = rootNode
        return rootNode
    }

    /**
     * Returns the TreeItem associated to the given group.
     * If the TreeItem does not exist it is created and added to the TreeView.
     */
    private fun getGroupItem(group: String, rootNode: TreeItem<Any> = treeView.root): TreeItem<Any> {
        fun createGroupItem(group: String, parent: TreeItem<Any>): TreeItem<Any> {
            val item: TreeItem<Any> = TreeItem(group)
            parent.children.add(item)
            return item
        }

        if (group == "/") {
            groupItems["/"] = rootNode
            return rootNode
        } else if (!group.contains('/')) {
            return groupItems.computeIfAbsent(group) { createGroupItem(group, rootNode) }
        } else {
            var previousSlash = 0
            var childGroupItem: TreeItem<Any> = rootNode
            while (true) {
                val nextSlash = group.indexOf('/', previousSlash)
                val nextGroupEnd = if (nextSlash == -1) group.length else nextSlash

                val groupPath = group.substring(0, nextGroupEnd)
                childGroupItem = groupItems.computeIfAbsent(groupPath) {
                    val groupName = group.substring(previousSlash, nextGroupEnd)
                    createGroupItem(groupName, childGroupItem)
                }

                if (nextSlash == -1) {
                    break
                }

                previousSlash = nextSlash + 1
            }

            return childGroupItem
        }
    }

    private fun addToGroups(shelfItem: ShelfItem, additionalAction: ((shelfTreeItem: TreeItem<Any>, group: String) -> Unit)? = null) {
        shelfItem.groups.forEach { group ->
            val normalizedGroup = normalizeGroup(group)
            val shelfTreeItem: TreeItem<Any> = TreeItem(shelfItem)
            itemsByGroup.computeIfAbsent(normalizedGroup) { mutableListOf() }
                    .add(shelfTreeItem)
            additionalAction?.invoke(shelfTreeItem, normalizedGroup)
        }
    }

    private fun removeFromGroups(shelfItem: ShelfItem) {
        shelfItem.groups.forEach { group ->
            val normalizedGroup = normalizeGroup(group)
            val groupItem = groupItems[normalizedGroup] ?: return@forEach
            groupItem.children.removeIf { (it.value as? ShelfItem)?.id == shelfItem.id }
            if (groupItem.children.isEmpty()) {
                groupItem.parent?.children?.remove(groupItem)
            }
        }
    }

    fun insertItem(item: ShelfItem) {
        addToGroups(item) { shelfTreeItem, group -> getGroupItem(group).children.add(shelfTreeItem) }
    }

    fun removeItem(shelfItem: ShelfItem) {
        removeFromGroups(shelfItem)
    }
}
