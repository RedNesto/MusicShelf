package io.github.rednesto.musicshelf.serialization

import io.github.rednesto.musicshelf.ShelfItem
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.xml.XmlConfigurationLoader
import java.nio.file.Path

object ShelfItemStorage {

    const val DEFAULT_FILE_NAME: String = "items.xml"

    fun load(filePath: Path): List<ShelfItem> {
        val configLoader = createLoader(filePath)
        val loadedNode = configLoader.load()
        return loadedNode.getList(TypeTokens.SHELF_ITEM) { emptyList() }.filterNotNull()
    }

    fun save(items: List<ShelfItem>, filePath: Path) {
        val configLoader = createLoader(filePath)
        val nodeToSave = configLoader.createNode().set(TypeTokens.SHELF_ITEM_LIST, items)
        configLoader.save(nodeToSave)
    }

    private fun createLoader(filePath: Path): XmlConfigurationLoader {
        val options = ConfigurationOptions.defaults()
                .serializers { builder ->
                    builder.register(TypeTokens.SHELF_ITEM, ShelfItemTypeSerializer())
                            .register(TypeTokens.PATH, PathTypeSerializer())
                }

        return XmlConfigurationLoader.builder()
                .defaultOptions(options)
                .path(filePath)
                .includesXmlDeclaration(true)
                .build()
    }
}
