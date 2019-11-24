package io.github.rednesto.musicshelf.projectFilesCollectors

import io.github.rednesto.musicshelf.MusicShelfBundle
import io.github.rednesto.musicshelf.ProjectFilesCollector
import io.github.rednesto.musicshelf.serialization.TypeTokens
import io.github.rednesto.musicshelf.ui.ProjectFilesTableViewHelper
import io.github.rednesto.musicshelf.utils.getItemNameForPath
import io.github.rednesto.musicshelf.utils.renameToAvoidDuplicates
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.input.KeyCode
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.FileChooser
import ninja.leaping.configurate.ConfigurationNode
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

class ListedProjectFilesCollector : ProjectFilesCollector {

    val filesNameColumn = TableColumn<Pair<String, Path>, String>(MusicShelfBundle.get("create.project.files.header.name"))
    val filesPathColumn = TableColumn<Pair<String, Path>, Path>(MusicShelfBundle.get("create.project.files.header.path"))
    val filesTableView: TableView<Pair<String, Path>> = TableView<Pair<String, Path>>().apply {
        isEditable = true
        columns.addAll(filesNameColumn, filesPathColumn)
        ProjectFilesTableViewHelper.configure(this, filesNameColumn, filesPathColumn)

    }

    override val id: String = "listed_files"

    override fun getDisplayname(locale: Locale): String =
            MusicShelfBundle.get("project.files_collectors.listed_files")

    override fun createConfigurationNode(): Node {
        filesTableView.placeholder = Text(MusicShelfBundle.get("create.project.files.placeholder"))
        filesTableView.setOnKeyPressed { event ->
            if (event.code == KeyCode.DELETE) {
                filesTableView.items.removeAll(filesTableView.selectionModel.selectedItems)
            }
        }
        val filesLabel = Label(MusicShelfBundle.get("create.project.files"))
        filesLabel.labelFor = filesTableView
        filesLabel.isMnemonicParsing = true

        val addFileButton = Button(MusicShelfBundle.get("create.project.file.add")).apply {
            setOnAction {
                val fileChooser = FileChooser().apply {
                    title = MusicShelfBundle.get("create.project.file.file_choose_title")
                }
                val selectedFiles = fileChooser.showOpenMultipleDialog(filesTableView.scene.window)
                        ?: return@setOnAction
                selectedFiles.mapNotNullTo(filesTableView.items) { file ->
                    val path = file.toPath()
                    val name = renameToAvoidDuplicates(getItemNameForPath(path), filesTableView.items.map { it.first })
                    name to path
                }
            }
        }
        val removeFileButton = Button(MusicShelfBundle.get("create.project.file.remove")).apply {
            setOnAction { filesTableView.items.removeAll(filesTableView.selectionModel.selectedItems) }
        }
        val buttonsBox = HBox(addFileButton, removeFileButton).apply {
            padding = Insets(5.0, 0.0, 0.0, 0.0)
            spacing = 10.0
        }

        filesTableView.items.clear()
        files?.toList()?.let(filesTableView.items::addAll)

        return VBox(filesLabel, filesTableView, buttonsBox)
    }

    private var files: Map<String, Path>? = null

    override fun applyConfiguration() {
        files = filesTableView.items.toMap()
    }

    override fun loadConfiguration(configurationNode: ConfigurationNode) {
        files = configurationNode.getList(TypeTokens.STRINGS_MAP).mapNotNull { map ->
            val fileName = map["name"] ?: return@mapNotNull null
            val filePath = map["path"] ?: return@mapNotNull null
            fileName to Paths.get(filePath)
        }.toMap()
    }

    override fun saveConfiguration(configurationNode: ConfigurationNode) {
        files?.let { it ->
            configurationNode.value = it.map { (name, path) ->
                mapOf("name" to name, "path" to path.toAbsolutePath().toString())
            }
        }
    }

    override fun collect(): Map<String, Path> = files ?: emptyMap()
}
