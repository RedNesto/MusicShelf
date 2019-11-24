package io.github.rednesto.musicshelf.projectFilesCollectors

import io.github.rednesto.musicshelf.MusicShelfBundle
import io.github.rednesto.musicshelf.ProjectFilesCollector
import io.github.rednesto.musicshelf.utils.getItemNameForPath
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import ninja.leaping.configurate.ConfigurationNode
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors

class DirectoryProjectFilesCollector : ProjectFilesCollector {

    private val directoryPathField: TextField = TextField()
    private val filterField: TextField = TextField()

    override val id: String = "directory_content"

    override fun getDisplayname(locale: Locale): String =
            MusicShelfBundle.get("project.files_collectors.directory_based")

    override fun createConfigurationNode(): Node {
        val directoryPathLabel = Label(MusicShelfBundle.get("project.files_collectors.directory_based.directory"))
        directoryPathLabel.labelFor = filterField
        directoryPathField.text = directoryPath?.toString()

        val filterLabel = Label(MusicShelfBundle.get("project.files_collectors.directory_based.filter"))
        filterLabel.labelFor = filterField
        filterField.text = fileFilter

        return VBox(directoryPathLabel, directoryPathField, filterLabel, filterField).apply { spacing = 5.0 }
    }

    private var directoryPath: Path? = null
    private var fileFilter: String? = null

    override fun applyConfiguration() {
        directoryPath = Paths.get(directoryPathField.text)
        fileFilter = filterField.text
    }

    override fun loadConfiguration(configurationNode: ConfigurationNode) {
        directoryPath = configurationNode.getNode("directory").string?.let { Paths.get(it) }
        fileFilter = configurationNode.getNode("filter").string
    }

    override fun saveConfiguration(configurationNode: ConfigurationNode) {
        configurationNode.getNode("directory").value = directoryPath.toString()
        configurationNode.getNode("filter").value = fileFilter
    }

    override fun collect(): Map<String, Path> {
        val dir = directoryPath
        if (dir == null || !Files.isDirectory(dir)) {
            return emptyMap()
        }

        val compiledFilter = fileFilter?.toRegex()

        val keyExtractor = Function { file: Path -> getItemNameForPath(file) }
        return Files.list(dir).use { stream ->
            stream.filter { path ->
                Files.isRegularFile(path) && (compiledFilter == null || path.fileName.toString().matches(compiledFilter))
            }.collect(Collectors.toUnmodifiableMap<Path, String, Path>(keyExtractor, Function.identity()))
        }
    }
}
