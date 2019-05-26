package io.github.rednesto.musicshelf

import java.nio.file.Path
import java.util.*
import kotlin.reflect.KProperty

data class ShelfItem(val id: UUID, val path: Path, val infos: MutableMap<String, String>)

var ShelfItem.name: String? by ShelfItemInfoDelegate(ShelfItemInfoKeys.NAME)

object ShelfItemInfoKeys {
    const val NAME = "name"

    val ALL: List<String> = listOf(NAME)
}

class ShelfItemInfoDelegate(private val infoKey: String) {
    operator fun getValue(backed: ShelfItem, property: KProperty<*>): String? {
        return backed.infos[infoKey]
    }

    operator fun setValue(backed: ShelfItem, property: KProperty<*>, value: String?) {
        if (value == null) {
            backed.infos.remove(ShelfItemInfoKeys.NAME)
        } else {
            backed.infos[ShelfItemInfoKeys.NAME] = value
        }
    }
}
