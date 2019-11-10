package io.github.rednesto.musicshelf.ui

import io.github.rednesto.musicshelf.*
import io.github.rednesto.musicshelf.utils.addIfAbsent
import io.github.rednesto.musicshelf.utils.isRootGroup
import io.github.rednesto.musicshelf.utils.normalizeGroup
import javafx.beans.value.WeakChangeListener
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView

class ShelfTreeViewHelper(val treeView: TreeView<Any>, val shelf: Shelf) {

    private var filter: ShelfItemFilter? = null

    val rootItem: ShelfTreeRootItem = ShelfTreeRootItem()
    /** Root node used to display search results */
    private var filteredRootItem: ShelfTreeRootItem = ShelfTreeRootItem().apply { alwaysRememberExpandedGroups = true }

    fun recreateRootNode(): TreeItem<Any> {
        val newRoot = rootItem.recreate(shelf.getAllItems())
        treeView.root = newRoot
        return newRoot
    }

    fun insertItem(item: ShelfItem) {
        rootItem.addItem(item)
        if (this.filter != null) {
            treeView.root = filteredRootItem.recreate(filterItems())
        }
    }

    fun removeItem(item: ShelfItem) {
        rootItem.removeItem(item)
        if (this.filter != null) {
            treeView.root = filteredRootItem.recreate(filterItems())
        }
    }

    fun filter(filter: String?) {
        if (filter.isNullOrBlank()) {
            this.filter = null
            filteredRootItem.forgetExpandedGroups()
            treeView.root = rootItem.treeItem
        } else {
            this.filter = ShelfItemFilter(ShelfItemFilterDataParser.parseFilter(filter))
            treeView.root = filteredRootItem.recreate(filterItems())
        }
    }

    fun clearFilter() {
        filter(null)
    }

    private fun filterItems(): Collection<ShelfItem> {
        val filter = this.filter ?: return shelf.getAllItems()
        return shelf.getAllItems().filter(filter)
    }
}

class ShelfTreeRootItem {

    var treeItem: TreeItem<Any>? = null
        private set

    var alwaysRememberExpandedGroups: Boolean = false
    private val expandedGroups = mutableSetOf<String>()

    private val groupItems = mutableMapOf<String, TreeItem<Any>>()
    private val itemsByGroup = mutableMapOf<String, MutableList<TreeItem<Any>>>()

    fun recreate(items: Collection<ShelfItem>): TreeItem<Any> {
        treeItem = null
        if (!alwaysRememberExpandedGroups) {
            expandedGroups.clear()
        }
        groupItems.clear()
        itemsByGroup.clear()

        val rootNode = TreeItem<Any>()

        // Group all items of the shelf
        items.forEach { addToGroups(it) }

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
        itemsByGroup.forEach { (fullGroup, items) ->
            val groupItem = groupItems[fullGroup] ?: rootNode
            addChildrenAndSort(groupItem, items)
        }

        treeItem = rootNode
        return rootNode
    }

    /**
     * Returns the TreeItem associated to the given group.
     * If the TreeItem does not exist it is created and added to the TreeView.
     */
    private fun getGroupItem(group: String, rootNode: TreeItem<Any>): TreeItem<Any> {
        fun TreeItem<Any>.trackAndRestoreExpansion() {
            val itemGroup = this.value as String
            this.expandedProperty().addListener(WeakChangeListener { _, _, newValue ->
                if (newValue) {
                    expandedGroups.add(itemGroup)
                } else {
                    expandedGroups.remove(itemGroup)
                }
            })
            this.isExpanded = expandedGroups.contains(itemGroup)
        }

        if (group == "/") {
            groupItems["/"] = rootNode
            return rootNode
        } else if (!group.contains('/')) {
            return groupItems.computeIfAbsent(group) {
                TreeItem<Any>(group).apply {
                    addChildAndSort(rootNode, this)
                    trackAndRestoreExpansion()
                }
            }
        } else {
            var previousSlash = 0
            var childGroupItem: TreeItem<Any> = rootNode
            while (true) {
                val nextSlash = group.indexOf('/', previousSlash)
                val nextGroupEnd = if (nextSlash == -1) group.length else nextSlash

                val groupPath = group.substring(0, nextGroupEnd)
                childGroupItem = groupItems.computeIfAbsent(groupPath) {
                    val groupName = group.substring(previousSlash, nextGroupEnd)
                    TreeItem<Any>(groupName).apply {
                        addChildAndSort(childGroupItem, this)
                        trackAndRestoreExpansion()
                    }
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
        val addAction: (String) -> Unit = { group ->
            val normalizedGroup = normalizeGroup(group)
            val shelfTreeItem: TreeItem<Any> = TreeItem(shelfItem)
            itemsByGroup.computeIfAbsent(normalizedGroup) { mutableListOf() }
                    .addIfAbsent(shelfTreeItem)
            additionalAction?.invoke(shelfTreeItem, normalizedGroup)
        }

        if (shelfItem.groups.isEmpty()) {
            addAction("/")
        } else {
            shelfItem.groups.forEach(addAction)
        }
    }

    private fun removeFromGroups(shelfItem: ShelfItem) {
        val removeAction: (String) -> Unit = { group ->
            val normalizedGroup = normalizeGroup(group)
            groupItems[normalizedGroup]?.let { groupItem ->
                groupItem.children.removeIf { (it.value as? ShelfItem)?.id == shelfItem.id }
                if (groupItem.children.isEmpty()) {
                    groupItem.parent?.children?.remove(groupItem)
                }
            }
        }

        if (shelfItem.groups.isEmpty()) {
            removeAction("/")
        } else {
            shelfItem.groups.forEach(removeAction)
        }
    }

    fun addItem(item: ShelfItem) {
        val rootNode = this.treeItem
        checkNotNull(rootNode)
        addToGroups(item) { shelfTreeItem, group ->
            val groupItem = getGroupItem(group, rootNode)
            addChildAndSort(groupItem, shelfTreeItem)

            if (!isRootGroup(group) && groupItem.parent == null) {
                // If this group's parent is the root group its path does not contain a slash,
                // thus we need to fallback to the root path
                val groupParentItem = getGroupItem(group.substringBeforeLast('/', "/"), rootNode)
                addChildAndSort(groupParentItem, groupItem)
            }
        }
    }

    fun removeItem(shelfItem: ShelfItem) {
        removeFromGroups(shelfItem)
    }

    fun forgetExpandedGroups() {
        expandedGroups.clear()
    }
}

/**
 * Default comparator used to sort items on a shelf TreeView.
 *
 * Groups are always on top, sorted by natural order of their name
 * Items (leafs) are always under groups, sorted by natural order of their name
 */
object ShelfItemComparator : Comparator<TreeItem<Any>> { // TODO I want my own tests :(
    override fun compare(o1: TreeItem<Any>, o2: TreeItem<Any>): Int {
        val value1 = o1.value
        val value2 = o2.value
        return if (value1 is String && value2 is String) {
            value1.compareTo(value2, true)
        } else if (value1 is ShelfItem && value2 is ShelfItem) {
            value1.nameOrUnnamed.compareTo(value2.nameOrUnnamed, true)
        } else if (value1 is String) {
            -1
        } else 1
    }
}

private fun addChildAndSort(parent: TreeItem<Any>, child: TreeItem<Any>) {
    if (parent.children.addIfAbsent(child)) {
        sortItems(parent)
    }
}

private fun addChildrenAndSort(parent: TreeItem<Any>, children: Collection<TreeItem<Any>>) {
    // Removes all duplicates from the children collection as well as those already present in the parent
    val uniqueChildren = children.toMutableSet().apply { removeAll(parent.children) }
    if (uniqueChildren.isNotEmpty() && parent.children.addAll(uniqueChildren)) {
        sortItems(parent)
    }
}

private fun sortItems(item: TreeItem<Any>) {
    item.children.sortWith(ShelfItemComparator)
}
