package io.github.rednesto.musicshelf

import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet

data class ShelfItem(val id: UUID, val path: Path, val info: Map<String, String>, val groups: Set<String>)

val ShelfItem.name: String? get() = this.info[ShelfItemInfoKeys.NAME]
val ShelfItem.nameOrUnnamed: String get() = this.name ?: MusicShelfBundle.get("shelf.item.unnamed")

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
        return ShelfItem(UUID.randomUUID(), path.toAbsolutePath(), unmodifiableInfo, unmodifiableGroups)
    }
}
