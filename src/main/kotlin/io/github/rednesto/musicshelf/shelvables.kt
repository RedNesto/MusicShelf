package io.github.rednesto.musicshelf

import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet

sealed class Shelvable {
    abstract val id: UUID
    abstract val name: String
    abstract val groups: Set<String>
    abstract val info: Map<String, String>
}

data class ShelfItem(
        override val id: UUID,
        override val info: Map<String, String>,
        override val groups: Set<String>,
        val path: Path
) : Shelvable() {
    override val name: String
        get() = this.info[ShelfItemInfoKeys.NAME] ?: MusicShelfBundle.get("shelf.item.unnamed")
}

object ShelfItemInfoKeys {
    const val NAME = "name"

    val ALL: List<String> = listOf(NAME)

    val DEFAULT_VALUES: Map<String, String> = mapOf(
            NAME to "A new item"
    )
}

object ShelfItemFactory {
    fun create(path: Path, groups: Set<String> = emptySet(), info: Map<String, String> = emptyMap()): ShelfItem {
        if (!Files.isRegularFile(path)) {
            throw IllegalStateException("Cannot create a ShelfItem whose path does not exists or is not a file.")
        }

        val unmodifiableInfo = Collections.unmodifiableMap(HashMap(info))
        val unmodifiableGroups = Collections.unmodifiableSet(HashSet(groups))
        return ShelfItem(UUID.randomUUID(), unmodifiableInfo, unmodifiableGroups, path.toAbsolutePath())
    }
}
