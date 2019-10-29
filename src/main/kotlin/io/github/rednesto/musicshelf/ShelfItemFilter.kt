package io.github.rednesto.musicshelf

import java.util.function.Predicate

class ShelfItemFilter(val filterData: ShelfItemFilterData) : Predicate<ShelfItem>, (ShelfItem) -> Boolean {

    override fun test(t: ShelfItem): Boolean {
        val matchesKeywords = filterData.keywords.isEmpty() || filterData.keywords.any { t.nameOrUnnamed.contains(it, true) }
        val matchesInfo = filterData.infoToSearch.isEmpty() || filterData.infoToSearch.any { entry ->
            if (entry.value == null) t.infos.containsKey(entry.key) else t.infos[entry.key] == entry.value
        }
        return matchesKeywords && matchesInfo
    }

    override fun invoke(p1: ShelfItem): Boolean = test(p1)

    fun filter(items: Collection<ShelfItem>): List<ShelfItem> = items.filter(this)
}
