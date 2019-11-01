package io.github.rednesto.musicshelf

import io.github.rednesto.musicshelf.utils.isRootGroup
import java.util.function.Predicate

class ShelfItemFilter(val filterData: ShelfItemFilterData) : Predicate<ShelfItem>, (ShelfItem) -> Boolean {

    override fun test(t: ShelfItem): Boolean {
        val matchesKeywords = filterData.keywords.isEmpty() || filterData.keywords.any { t.nameOrUnnamed.contains(it, true) }
        val matchesGroups = filterData.groups.isEmpty() || filterData.groups.any { group ->
            if (t.groups.isEmpty()) {
                isRootGroup(group)
            } else if (group.endsWith('/') && !isRootGroup(group)) {
                t.groups.any { it.startsWith(group.removeSuffix("/")) }
            } else {
                t.groups.contains(group)
            }
        }
        val matchesInfo = filterData.info.isEmpty() || filterData.info.any { entry ->
            if (entry.value == null) t.info.containsKey(entry.key) else t.info[entry.key] == entry.value
        }
        return matchesKeywords && matchesGroups && matchesInfo
    }

    override fun invoke(p1: ShelfItem): Boolean = test(p1)

    fun filter(items: Collection<ShelfItem>): List<ShelfItem> = items.filter(this)
}
