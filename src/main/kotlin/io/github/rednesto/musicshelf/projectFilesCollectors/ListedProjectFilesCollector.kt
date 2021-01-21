package io.github.rednesto.musicshelf.projectFilesCollectors

import io.github.rednesto.musicshelf.MusicShelfBundle
import io.github.rednesto.musicshelf.ProjectFilesCollector
import io.github.rednesto.musicshelf.serialization.TypeTokens
import io.github.rednesto.musicshelf.ui.ProjectFilesTableViewHelper
import io.github.rednesto.musicshelf.utils.configureFxmlLoader
import io.github.rednesto.musicshelf.utils.getItemNameForPath
import io.github.rednesto.musicshelf.utils.renameToAvoidDuplicates
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.stage.FileChooser
import org.spongepowered.configurate.ConfigurationNode
import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

class ListedProjectFilesCollector : ProjectFilesCollector {

    private val configNode: Node
    private val configController: ConfigController

    init {
        val fxmlLoader = configureFxmlLoader("/ui/filesCollectors/ListedFilesCollectorConfig.fxml", resources = MusicShelfBundle.getBundle())
        configNode = fxmlLoader.load()
        configController = fxmlLoader.getController()!!
    }

    override val id: String = "listed_files"

    override fun getDisplayname(locale: Locale): String =
            MusicShelfBundle.get("project.files_collectors.listed_files")

    override fun createConfigurationNode(): Node {
        configController.filesTableView.items.clear()
        files?.toList()?.let(configController.filesTableView.items::addAll)
        return configNode
    }

    private var files: Map<String, Path>? = null

    override fun applyConfiguration() {
        files = configController.filesTableView.items.toMap()
    }

    override fun loadConfiguration(configurationNode: ConfigurationNode) {
        files = configurationNode.getList(TypeTokens.STRINGS_MAP) { emptyList() }.mapNotNull { map ->
            val fileName = map["name"] ?: return@mapNotNull null
            val filePath = map["path"] ?: return@mapNotNull null
            fileName to Paths.get(filePath)
        }.toMap()
    }

    override fun saveConfiguration(configurationNode: ConfigurationNode) {
        files?.let { it ->
            configurationNode.set(it.map { (name, path) ->
                mapOf("name" to name, "path" to path.toAbsolutePath().toString())
            })
        }
    }

    override fun transferConfigurationTo(other: ProjectFilesCollector): Boolean {
        return if (other is ListedProjectFilesCollector) {
            other.files = this.files
            true
        } else false
    }

    override fun collect(): Map<String, Path> = files ?: emptyMap()

    class ConfigController : Initializable {
        @FXML
        lateinit var filesLabel: Label
        @FXML
        lateinit var filesTableView: TableView<Pair<String, Path>>
        @FXML
        lateinit var filesNameColumn: TableColumn<Pair<String, Path>, String>
        @FXML
        lateinit var filesPathColumn: TableColumn<Pair<String, Path>, Path>

        override fun initialize(location: URL?, resources: ResourceBundle?) {
            filesLabel.labelFor = filesTableView
            ProjectFilesTableViewHelper.configure(filesTableView, filesNameColumn, filesPathColumn)
        }

        @FXML
        fun filesTableView_onKeyPressed(event: KeyEvent) {
            if (event.code == KeyCode.DELETE) {
                filesTableView.items.removeAll(filesTableView.selectionModel.selectedItems)
            }
        }

        @FXML
        fun addFileButton_onAction(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
            val fileChooser = FileChooser().apply {
                title = MusicShelfBundle.get("project.files_collectors.listed_files.file_chooser.title")
            }
            val selectedFiles = fileChooser.showOpenMultipleDialog(filesTableView.scene.window)
                    ?: return
            selectedFiles.mapNotNullTo(filesTableView.items) { file ->
                val path = file.toPath()
                val name = renameToAvoidDuplicates(getItemNameForPath(path), filesTableView.items.map { it.first })
                name to path
            }
        }

        @FXML
        fun removeFileButton_onAction(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
            filesTableView.items.removeAll(filesTableView.selectionModel.selectedItems)
        }
    }
}
