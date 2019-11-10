package io.github.rednesto.musicshelf.serialization

import io.github.rednesto.musicshelf.ShelfItem
import ninja.leaping.configurate.ConfigurationOptions
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers
import ninja.leaping.configurate.xml.XMLConfigurationLoader
import java.nio.file.Path
import kotlin.streams.toList

object ShelfItemStorage {

    const val DEFAULT_FILE_NAME: String = "items.xml"

    fun load(filePath: Path): List<ShelfItem> {
        val configLoader = createLoader(filePath)
        val loadedNode = configLoader.load()
        val loadedList = loadedNode.getValue(TypeTokens.SHELF_ITEM_LIST)
        return if (loadedList != null) {
            // Configurate puts a null in the list if the file contains no items.
            // To avoid this issue and any other NPE we remove all nulls from the list
            loadedList.stream().filter { it != null }.toList()
        } else emptyList()
    }

    fun save(items: List<ShelfItem>, filePath: Path) {
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
