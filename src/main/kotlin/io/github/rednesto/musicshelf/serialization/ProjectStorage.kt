package io.github.rednesto.musicshelf.serialization

import io.github.rednesto.musicshelf.Project
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.xml.XmlConfigurationLoader
import java.nio.file.Path

object ProjectStorage {

    const val DEFAULT_FILE_NAME: String = "projects.xml"

    fun load(filePath: Path): List<Project> {
        val configLoader = createLoader(filePath)
        val loadedNode = configLoader.load()
        return loadedNode.getList(TypeTokens.PROJECT) { emptyList() }.filterNotNull()
    }

    fun save(projects: List<Project>, filePath: Path) {
        val configLoader = createLoader(filePath)
        val nodeToSave = configLoader.createNode().set(TypeTokens.PROJECT_LIST, projects)
        configLoader.save(nodeToSave)
    }

    private fun createLoader(filePath: Path): XmlConfigurationLoader {
        val options = ConfigurationOptions.defaults()
                .serializers { builder ->
                    builder.register(TypeTokens.PROJECT, ProjectTypeSerializer())
                            .register(TypeTokens.PATH, PathTypeSerializer())
                }

        return XmlConfigurationLoader.builder()
                .defaultOptions(options)
                .path(filePath)
                .includesXmlDeclaration(true)
                .build()
    }
}
