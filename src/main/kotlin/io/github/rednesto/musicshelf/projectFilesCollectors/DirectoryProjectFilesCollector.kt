package io.github.rednesto.musicshelf.projectFilesCollectors

import io.github.rednesto.musicshelf.MusicShelfBundle
import io.github.rednesto.musicshelf.ProjectFilesCollector
import io.github.rednesto.musicshelf.utils.configureFxmlLoader
import io.github.rednesto.musicshelf.utils.getItemNameForPath
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.stage.DirectoryChooser
import ninja.leaping.configurate.ConfigurationNode
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors

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
        directoryPath = Paths.get(configController.directoryPathField.text)
        fileFilter = configController.filterField.text
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
