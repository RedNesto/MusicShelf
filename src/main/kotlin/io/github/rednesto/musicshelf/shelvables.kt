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
        override val name: String,
        override val info: Map<String, String>,
        override val groups: Set<String>,
        val path: Path
) : Shelvable()

object ShelfItemInfoKeys {
    val ALL: List<String> = listOf()

    val DEFAULT_VALUES: Map<String, String> = mapOf()
}

object ShelfItemFactory {
    fun create(path: Path, name: String, groups: Set<String> = emptySet(), info: Map<String, String> = emptyMap()): ShelfItem {
        if (!Files.isRegularFile(path)) {
            throw IllegalStateException("Cannot create a ShelfItem whose path does not exists or is not a file.")
        }

        val unmodifiableInfo = Collections.unmodifiableMap(HashMap(info))
        val unmodifiableGroups = Collections.unmodifiableSet(HashSet(groups))
        return ShelfItem(UUID.randomUUID(), name, unmodifiableInfo, unmodifiableGroups, path.toAbsolutePath())
    }
}

data class Project(
        override val id: UUID,
        override val name: String,
        override val groups: Set<String>,
        override val info: Map<String, String>,
        val files: Map<String, Path>
) : Shelvable()
