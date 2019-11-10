package io.github.rednesto.musicshelf.serialization

import io.github.rednesto.musicshelf.ShelfItem
import ninja.leaping.configurate.ConfigurationOptions
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers
import ninja.leaping.configurate.xml.XMLConfigurationLoader
import java.nio.file.Path

object ShelfItemStorage {

    const val DEFAULT_FILE_NAME: String = "items.xml"

    fun load(filePath: Path): List<ShelfItem> {
        val configLoader = createLoader(filePath)
        val loadedNode = configLoader.load()
        return loadedNode.getList(TypeTokens.SHELF_ITEM).filterNotNull()
    }

    fun save(items: List<ShelfItem>, filePath: Path) {
        val configLoader = createLoader(filePath)
        val nodeToSave = configLoader.createEmptyNode().setValue(TypeTokens.SHELF_ITEM_LIST, items)
        configLoader.save(nodeToSave)
    }

    private fun createLoader(filePath: Path): XMLConfigurationLoader {
        val serializers = TypeSerializers.newCollection()
                .registerType(TypeTokens.SHELF_ITEM, ShelfItemTypeSerializer())
                .registerType(TypeTokens.PATH, PathTypeSerializer())
        val options = ConfigurationOptions.defaults()
                .setSerializers(serializers)

        return XMLConfigurationLoader.builder()
                .setDefaultOptions(options)
                .setPath(filePath)
                .setIncludeXmlDeclaration(true)
                .build()
    }
}
