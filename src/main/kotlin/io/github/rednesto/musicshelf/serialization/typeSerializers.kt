package io.github.rednesto.musicshelf.serialization

import com.google.common.reflect.TypeToken
import io.github.rednesto.musicshelf.ShelfItem
import ninja.leaping.configurate.ConfigurationNode
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer
import java.nio.file.Paths
import java.util.*

object TypeTokens {
    val STRINGS_MAP: TypeToken<Map<String, String>> = object : TypeToken<Map<String, String>>() {}
    val SHELF_ITEM: TypeToken<ShelfItem> = TypeToken.of(ShelfItem::class.java)
    val SHELF_ITEM_LIST: TypeToken<List<ShelfItem>> = object : TypeToken<List<ShelfItem>>() {}
}

class ShelfItemTypeSerializer : TypeSerializer<ShelfItem> {
    override fun deserialize(type: TypeToken<*>, value: ConfigurationNode): ShelfItem? {
        val id = UUID.fromString(value.getNode("id").string ?: return null)
        val path = Paths.get(value.getNode("path").string ?: return null)
        val info = (value.getNode("info").getValue(TypeTokens.STRINGS_MAP) ?: return null)

        return ShelfItem(id, path, info.toMutableMap())
    }

    override fun serialize(type: TypeToken<*>, obj: ShelfItem?, value: ConfigurationNode) {
        if (obj == null) {
            return
        }

        value.getNode("id").value = obj.id.toString()
        value.getNode("path").value = obj.path.toAbsolutePath().toString()
        value.getNode("info").value = obj.infos
    }
}
