package io.github.rednesto.musicshelf.projectFilesCollectors

import io.github.rednesto.musicshelf.MusicShelfBundle
import io.github.rednesto.musicshelf.ProjectFilesCollector
import io.github.rednesto.musicshelf.utils.configureFxmlLoader
import io.github.rednesto.musicshelf.utils.getExtensionForPath
import io.github.rednesto.musicshelf.utils.getItemNameForPath
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.stage.DirectoryChooser
import org.spongepowered.configurate.ConfigurationNode
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

class DirectoryProjectFilesCollector : ProjectFilesCollector {

    private val configNode: Node
    private val configController: ConfigController

    init {
        val fxmlLoader = configureFxmlLoader("/ui/filesCollectors/DirectoryCollectorConfig.fxml", resources = MusicShelfBundle.getBundle())
        configNode = fxmlLoader.load()
        configController = fxmlLoader.getController()!!
    }

    override val id: String = "directory_content"

    override fun getDisplayname(locale: Locale): String =
            MusicShelfBundle.get("project.files_collectors.directory_based")

    override fun createConfigurationNode(): Node {
        configController.directoryPathField.text = directoryPath?.toString()
        configController.filterField.text = fileFilter
        return configNode
    }

    private var directoryPath: Path? = null
    private var fileFilter: String? = null

    override fun applyConfiguration() {
        directoryPath = configController.directoryPathField.text?.let { if (it.isBlank()) null else Paths.get(it) }
        fileFilter = configController.filterField.text?.ifBlank { null }
    }

    override fun loadConfiguration(configurationNode: ConfigurationNode) {
        directoryPath = configurationNode.node("directory").string?.let { Paths.get(it) }
        fileFilter = configurationNode.node("filter").string?.ifBlank { null }
    }

    override fun saveConfiguration(configurationNode: ConfigurationNode) {
        configurationNode.node("directory").set(directoryPath.toString())
        configurationNode.node("filter").set(fileFilter)
    }

    override fun transferConfigurationTo(other: ProjectFilesCollector): Boolean {
        return if (other is DirectoryProjectFilesCollector) {
            other.directoryPath = this.directoryPath
            other.fileFilter = this.fileFilter
            true
        } else false
    }

    override fun collect(): Map<String, Path> {
        val dir = directoryPath
        if (dir == null || !Files.isDirectory(dir)) {
            return emptyMap()
        }

        val compiledFilter = fileFilter?.toRegex()

        val conflictingBaseNames = mutableSetOf<String>()
        val mappedFiles = mutableMapOf<String, Path>()
        Files.list(dir).use { stream ->
            stream.forEach { path ->
                if (!Files.isRegularFile(path) || (compiledFilter != null && path.fileName.toString().matches(compiledFilter))) {
                    return@forEach
                }

                val suggestedName = getItemNameForPath(path)

                // We do not want duplicate names, so if there are files
                // with the same suggested name then we add the file
                // extension between parenthesis to have unique names
                // for the returned map.
                val existingMappedFile = mappedFiles.remove(suggestedName)?.also { conflictingBaseNames.add(suggestedName) }

                val mappedName = if (suggestedName in conflictingBaseNames) "$suggestedName (${getExtensionForPath(path)})" else suggestedName
                mappedFiles[mappedName] = path
                if (existingMappedFile != null) {
                    mappedFiles["$suggestedName (${getExtensionForPath(existingMappedFile)})"] = existingMappedFile
                }
            }
        }

        return mappedFiles
    }

    class ConfigController : Initializable {
        @FXML
        lateinit var directoryPathLabel: Label
        @FXML
        lateinit var directoryPathField: TextField

        @FXML
        lateinit var filterLabel: Label
        @FXML
        lateinit var filterField: TextField

        @FXML
        fun selectDirectoryButton_onAction(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
            val selectedDir = DirectoryChooser().apply {
                title = MusicShelfBundle.get("project.files_collectors.directory_based.dir_chooser.title")
                val currentDir = directoryPathField.text
                if (!currentDir.isNullOrBlank()) {
                    initialDirectory = File(currentDir)
                }
            }.showDialog(directoryPathField.scene.window) ?: return
            directoryPathField.text = selectedDir.absolutePath
        }

        override fun initialize(location: URL?, resources: ResourceBundle?) {
            directoryPathLabel.labelFor = filterField
            filterLabel.labelFor = filterField
        }
    }
}
