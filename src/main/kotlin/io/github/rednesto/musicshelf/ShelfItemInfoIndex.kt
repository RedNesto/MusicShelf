package io.github.rednesto.musicshelf

object ShelfItemInfoIndex {

    /**
     * The root Map has info keys as its keys, mapped to a set of known values.
     */
    private val index: MutableMap<String, MutableSet<String>> = mutableMapOf()

    fun indexShelf(shelf: Shelf) {
        shelf.getAllShelvables().forEach { item ->
            item.info.forEach { (key, value) ->
                val indexedValues = index.computeIfAbsent(key) { mutableSetOf() }
                indexedValues.add(value)
            }
        }
    }

    operator fun get(key: String): Set<String> = index[key] ?: emptySet()

    fun getAllKeys(): Set<String> = index.keys

    fun clear() = index.clear()
}
