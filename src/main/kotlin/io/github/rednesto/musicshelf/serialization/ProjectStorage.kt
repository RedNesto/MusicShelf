package io.github.rednesto.musicshelf.serialization

import io.github.rednesto.musicshelf.Project
import ninja.leaping.configurate.ConfigurationOptions
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers
import ninja.leaping.configurate.xml.XMLConfigurationLoader
import java.nio.file.Path

object ProjectStorage {

    const val DEFAULT_FILE_NAME: String = "projects.xml"

    fun load(filePath: Path): List<Project> {
        val configLoader = createLoader(filePath)
        val loadedNode = configLoader.load()
        return loadedNode.getList(TypeTokens.PROJECT).filterNotNull()
    }

    fun save(projects: List<Project>, filePath: Path) {
        val configLoader = createLoader(filePath)
        val nodeToSave = configLoader.createEmptyNode().setValue(projects)
        configLoader.save(nodeToSave)
    }

    private fun createLoader(filePath: Path): XMLConfigurationLoader {
        val serializers = TypeSerializers.newCollection()
                .registerType(TypeTokens.PROJECT, ProjectTypeSerializer())
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
