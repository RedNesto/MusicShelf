package io.github.rednesto.musicshelf.serialization

import io.github.rednesto.musicshelf.ShelfItem
import ninja.leaping.configurate.ConfigurationOptions
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers
import ninja.leaping.configurate.xml.XMLConfigurationLoader
import java.nio.file.Path
import java.nio.file.Paths

object ShelfItemStorage {

    val DEFAULT_FILE_PATH: Path = Paths.get("items.xml")

    fun load(filePath: Path = DEFAULT_FILE_PATH): List<ShelfItem> {
        val configLoader = createLoader(filePath)
        val loadedNode = configLoader.load()
        return loadedNode.getValue(TypeTokens.SHELF_ITEM_LIST) ?: emptyList()
    }

    fun save(items: List<ShelfItem>, filePath: Path = DEFAULT_FILE_PATH) {
        val configLoader = createLoader(filePath)
        val nodeToSave = configLoader.createEmptyNode().setValue(TypeTokens.SHELF_ITEM_LIST, items)
        configLoader.save(nodeToSave)
    }

    private fun createLoader(filePath: Path): XMLConfigurationLoader {
        val serializers = TypeSerializers.getDefaultSerializers()
                .newChild()
                .registerType(TypeTokens.SHELF_ITEM, ShelfItemTypeSerializer())
        val options = ConfigurationOptions.defaults()
                .setSerializers(serializers)

        return XMLConfigurationLoader.builder()
                .setDefaultOptions(options)
                .setPath(filePath)
                .setIncludeXmlDeclaration(true)
                .build()
    }
}
