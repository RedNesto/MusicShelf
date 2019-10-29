package io.github.rednesto.musicshelf.test

import io.github.rednesto.musicshelf.ShelfItem
import io.github.rednesto.musicshelf.ShelfItemFilter
import io.github.rednesto.musicshelf.ShelfItemFilterData
import io.github.rednesto.musicshelf.ShelfItemInfoKeys
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.file.Paths
import java.util.*

class ShelfItemFilterTest {

    private val itemsToTest = listOf(
            createItem("item0", setOf("groupA", "groupC/groupD")),
            createItem("items1", setOf("/", "groupA", "groupC/groupF"), "key2" to "value 1"),
            createItem("item2", emptySet(), "key1" to "value 1", "key2" to "value3"),
            createItem("item 3", setOf("groupB", "groupB/groupE"), "key1" to "value 1", "key 2" to "value: 2"),
            createItem("item4", emptySet(), "key1" to "value2", "key2" to "value3")
    )

    @Test
    fun `by keyword`() {
        assertEqualsUnordered(itemsToTest, filterKeywords("item"))
        assertEqualsUnordered(itemsToTest, filterKeywords("item", "1"))
        assertEqualsUnordered(getItems(1), filterKeywords("1"))
        assertEqualsUnordered(getItems(1), filterKeywords("ems", "1"))
        assertEqualsUnordered(getItems(3), filterKeywords("item 3"))
    }


    @Test
    fun `by group`() {
        assertEqualsUnordered(getItems(0, 1), filterGroups("groupA"))
        assertEqualsUnordered(getItems(1, 2, 4), filterGroups("/"))
        assertEqualsUnordered(getItems(3), filterGroups("groupB"))
        assertEqualsUnordered(getItems(0, 1, 3), filterGroups("groupA", "groupB"))
        assertEqualsUnordered(getItems(0, 1), filterGroups("groupC/"))
        assertEqualsUnordered(getItems(0), filterGroups("groupC/groupD"))
        assertEqualsUnordered(getItems(0, 1, 3), filterGroups("groupC/", "groupB"))
        assertEqualsUnordered(getItems(0, 1, 3), filterGroups("groupC/", "groupB/"))
    }

    @Test
    fun `by info`() {
        assertEqualsUnordered(getItems(2, 3), filterInfo("key1" to "value 1"))
        assertEqualsUnordered(getItems(3), filterInfo("key 2" to "value: 2"))
        assertEqualsUnordered(getItems(2, 3, 4), filterInfo("key1" to null))
        assertEqualsUnordered(getItems(2, 3, 4), filterInfo("key1" to null, "key 2" to null))
    }

    @Test
    fun mixed() {
        assertEquals(0, filter(setOf("item0"), emptySet(), "key1" to null).size)
        assertEqualsUnordered(getItems(2, 4), filter(setOf("item"), emptySet(), "key2" to "value3"))
        assertEqualsUnordered(getItems(1, 2, 4), filter(setOf("it", "ms"), emptySet(), "key2" to null))
    }

    private fun filterKeywords(vararg keywords: String): Collection<ShelfItem> =
            ShelfItemFilter(ShelfItemFilterData(keywords.toSet(), emptySet(), emptyMap())).filter(itemsToTest)

    private fun filterGroups(vararg groups: String): Collection<ShelfItem> =
            ShelfItemFilter(ShelfItemFilterData(emptySet(), groups.toSet(), emptyMap())).filter(itemsToTest)

    private fun filterInfo(vararg info: Pair<String, String?>): Collection<ShelfItem> =
            ShelfItemFilter(ShelfItemFilterData(emptySet(), emptySet(), info.toMap())).filter(itemsToTest)

    private fun filter(keywords: Set<String>, groups: Set<String>, vararg info: Pair<String, String?>): Collection<ShelfItem> =
            ShelfItemFilter(ShelfItemFilterData(keywords, groups, info.toMap())).filter(itemsToTest)

    private fun createItem(name: String, groups: Set<String>, vararg info: Pair<String, String>): ShelfItem {
        val infoMap = info.toMap(mutableMapOf(ShelfItemInfoKeys.NAME to name))
        return ShelfItem(UUID.randomUUID(), Paths.get(""), infoMap, groups.toMutableList())
    }

    private fun getItems(vararg indices: Int): List<ShelfItem> {
        val result = mutableListOf<ShelfItem>()
        for (index in indices) {
            result += itemsToTest[index]
        }
        return result
    }
}
