package io.github.rednesto.musicshelf.serialization

import io.github.rednesto.musicshelf.Project
import io.github.rednesto.musicshelf.ProjectFilesCollectorsLoader
import io.github.rednesto.musicshelf.ShelfItem
import io.github.rednesto.musicshelf.projectFilesCollectors.EmptyProjectFilesCollector
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

object TypeTokens {
    val STRING: TypeToken<String> = TypeToken.get(String::class.java)
    val PATH: TypeToken<Path> = TypeToken.get(Path::class.java)
    val STRINGS_MAP: TypeToken<Map<String, String>> = object : TypeToken<Map<String, String>>() {}
    val STRING_PATH_MAP: TypeToken<Map<String, Path>> = object : TypeToken<Map<String, Path>>() {}
    val SHELF_ITEM: TypeToken<ShelfItem> = TypeToken.get(ShelfItem::class.java)
    val SHELF_ITEM_LIST: TypeToken<List<ShelfItem>> = object : TypeToken<List<ShelfItem>>() {}
    val PROJECT: TypeToken<Project> = TypeToken.get(Project::class.java)
    val PROJECT_LIST: TypeToken<List<Project>> = object : TypeToken<List<Project>>() {}
}

class ShelfItemTypeSerializer : TypeSerializer<ShelfItem> {
    override fun deserialize(type: Type, value: ConfigurationNode): ShelfItem? {
        val id = UUID.fromString(value.node("id").string ?: return null)
        val name = value.node("name").string ?: return null
        val path = value.node("path").get(TypeTokens.PATH) ?: return null
        val info = (value.node("info").get(TypeTokens.STRINGS_MAP) ?: return null)
        val groups = value.node("groups").getList(TypeTokens.STRING) { emptyList() }.filter { !it.isNullOrEmpty() }

        return ShelfItem(id, name, info, groups.toSet(), path)
    }

    override fun serialize(type: Type, obj: ShelfItem?, value: ConfigurationNode) {
        if (obj == null) {
            return
        }

        value.node("id").set(obj.id.toString())
        value.node("name").set(obj.name)
        value.node("path").set(obj.path.toAbsolutePath().toString())
        value.node("info").set(obj.info)
        value.node("groups").set(obj.groups)
    }
}

class ProjectTypeSerializer : TypeSerializer<Project> {
    override fun deserialize(type: Type, value: ConfigurationNode): Project? {
        val id = UUID.fromString(value.node("id").string ?: return null)
        val name = value.node("name").string ?: return null
        val filesCollector = value.node("filesCollector").let { collectorNode ->
            collectorNode.node("id").string?.let { collectorId ->
                ProjectFilesCollectorsLoader.getCollector(collectorId)?.apply {
                    loadConfiguration(collectorNode.node("config"))
                }
            }
        } ?: EmptyProjectFilesCollector
        val info = value.node("info").get(TypeTokens.STRINGS_MAP) ?: return null
        val groups = value.node("groups").getList(TypeTokens.STRING) { emptyList() }.filterNot(String::isNullOrBlank)

        return Project(id, name, groups.toSet(), info, filesCollector)
    }

    override fun serialize(type: Type, obj: Project?, value: ConfigurationNode) {
        if (obj == null) {
            return
        }

        value.node("id").set(obj.id.toString())
        value.node("name").set(obj.name)
        val filesCollectorNode = value.node("filesCollector")
        filesCollectorNode.node("id").set(obj.filesCollector.id)
        obj.filesCollector.saveConfiguration(filesCollectorNode.node("config"))
        value.node("info").set(obj.info)
        value.node("groups").set(obj.groups)
    }
}

class PathTypeSerializer : TypeSerializer<Path> {
    override fun deserialize(type: Type, value: ConfigurationNode): Path? {
        val stringVal = value.string ?: return null
        return Paths.get(stringVal)
    }

    override fun serialize(type: Type, obj: Path?, value: ConfigurationNode) {
        if (obj != null) {
            value.set(obj.toString())
        }
    }
}
