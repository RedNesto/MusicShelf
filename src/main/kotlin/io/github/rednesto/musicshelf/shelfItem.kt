package io.github.rednesto.musicshelf

import java.nio.file.Path
import java.util.*
import kotlin.reflect.KProperty

data class ShelfItem(val id: UUID, val path: Path, val infos: MutableMap<String, String>, val groups: MutableList<String>)

var ShelfItem.name: String? by ShelfItemInfoDelegate(ShelfItemInfoKeys.NAME)
val ShelfItem.nameOrUnnamed: String
    get() = this.name ?: MusicShelfBundle.get("shelf.item.unnamed")

object ShelfItemInfoKeys {
    const val NAME = "name"

    val ALL: List<String> = listOf(NAME)

    val DEFAULT_VALUES: Map<String, String> = mapOf(
            NAME to "A new item"
    )
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
