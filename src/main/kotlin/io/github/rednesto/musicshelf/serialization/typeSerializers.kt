package io.github.rednesto.musicshelf.serialization

import com.google.common.reflect.TypeToken
import io.github.rednesto.musicshelf.Project
import io.github.rednesto.musicshelf.ShelfItem
import ninja.leaping.configurate.ConfigurationNode
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

object TypeTokens {
    val STRING: TypeToken<String> = TypeToken.of(String::class.java)
    val PATH: TypeToken<Path> = TypeToken.of(Path::class.java)
    val STRINGS_MAP: TypeToken<Map<String, String>> = object : TypeToken<Map<String, String>>() {}
    val STRING_PATH_MAP: TypeToken<Map<String, Path>> = object : TypeToken<Map<String, Path>>() {}
    val SHELF_ITEM: TypeToken<ShelfItem> = TypeToken.of(ShelfItem::class.java)
    val SHELF_ITEM_LIST: TypeToken<List<ShelfItem>> = object : TypeToken<List<ShelfItem>>() {}
    val PROJECT: TypeToken<Project> = TypeToken.of(Project::class.java)
    val PROJECT_LIST: TypeToken<List<Project>> = object : TypeToken<List<Project>>() {}
}

class ShelfItemTypeSerializer : TypeSerializer<ShelfItem> {
    override fun deserialize(type: TypeToken<*>, value: ConfigurationNode): ShelfItem? {
        val id = UUID.fromString(value.getNode("id").string ?: return null)
        val name = value.getNode("name").string ?: return null
        val path = value.getNode("path").getValue(TypeTokens.PATH) ?: return null
        val info = (value.getNode("info").getValue(TypeTokens.STRINGS_MAP) ?: return null)
        val groups = value.getNode("groups").getList(TypeTokens.STRING).filter { !it.isNullOrEmpty() }

        return ShelfItem(id, name, info, groups.toSet(), path)
    }

    override fun serialize(type: TypeToken<*>, obj: ShelfItem?, value: ConfigurationNode) {
        if (obj == null) {
            return
        }

        value.getNode("id").value = obj.id.toString()
        value.getNode("name").value = obj.name
        value.getNode("path").value = obj.path.toAbsolutePath().toString()
        value.getNode("info").value = obj.info
        value.getNode("groups").value = obj.groups
    }
}

class ProjectTypeSerializer : TypeSerializer<Project> {
    override fun deserialize(type: TypeToken<*>, value: ConfigurationNode): Project? {
        val id = UUID.fromString(value.getNode("id").string ?: return null)
        val name = value.getNode("name").string ?: return null
        val files = value.getNode("files").getValue(TypeTokens.STRING_PATH_MAP) ?: emptyMap()
        val info = (value.getNode("info").getValue(TypeTokens.STRINGS_MAP) ?: return null)
        val groups = value.getNode("groups").getList(TypeTokens.STRING).filter { !it.isNullOrEmpty() }

        return Project(id, name, groups.toSet(), info, files)
    }

    override fun serialize(type: TypeToken<*>, obj: Project?, value: ConfigurationNode) {
        if (obj == null) {
            return
        }

        value.getNode("id").value = obj.id.toString()
        value.getNode("name").value = obj.name
        value.getNode("files").value = obj.files
        value.getNode("info").value = obj.info
        value.getNode("groups").value = obj.groups
    }
}

class PathTypeSerializer : TypeSerializer<Path> {
    override fun deserialize(type: TypeToken<*>, value: ConfigurationNode): Path? {
        val stringVal = value.string ?: return null
        return Paths.get(stringVal)
    }

    override fun serialize(type: TypeToken<*>, obj: Path?, value: ConfigurationNode) {
        if (obj != null) {
            value.value = obj.toString()
        }
    }
}
