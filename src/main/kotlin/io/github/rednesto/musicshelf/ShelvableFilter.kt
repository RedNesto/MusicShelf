package io.github.rednesto.musicshelf

import io.github.rednesto.musicshelf.utils.isRootGroup
import java.util.function.Predicate

class ShelvableFilter(val filterData: ShelvableFilterData) : Predicate<Shelvable>, (Shelvable) -> Boolean {

    override fun test(t: Shelvable): Boolean {
        val matchesKeywords = filterData.keywords.isEmpty() || filterData.keywords.any { t.name.contains(it, true) }
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

    override fun invoke(p1: Shelvable): Boolean = test(p1)

    fun filter(items: Collection<Shelvable>): List<Shelvable> = items.filter(this)
}
