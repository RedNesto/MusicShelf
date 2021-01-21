package io.github.rednesto.musicshelf.appSupport.builtin

import io.github.rednesto.musicshelf.Configurable
import io.github.rednesto.musicshelf.ConfigurationException
import io.github.rednesto.musicshelf.MusicShelfBundle
import io.github.rednesto.musicshelf.appSupport.FileAppSupport
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import org.spongepowered.configurate.ConfigurationNode
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

abstract class ExecutableBasedAppSupport : FileAppSupport, Configurable {

    protected var executablePath: Path? = null

    abstract override fun getDisplayname(locale: Locale): String

    override fun supports(file: Path): Boolean = executablePath != null

    override fun open(file: Path) {
        if (executablePath == null) {
            return
        }

        ProcessBuilder(executablePath.toString(), file.toAbsolutePath().toString())
                .start()
    }

    private val executablePathTextField: TextField by lazy {
        TextField().apply { padding = Insets(5.0) }
    }
    private val configNode: VBox by lazy {
        HBox.setHgrow(executablePathTextField, Priority.ALWAYS)
        val choosePathButton = Button(MusicShelfBundle.get("app_support.executable_based.choose_executable_path"))
        choosePathButton.setOnAction {
            val selectedFile = FileChooser().apply {
                title = MusicShelfBundle.get("app_support.executable_based.choose_executable_path.title")
            }.showOpenDialog(choosePathButton.scene.window) ?: return@setOnAction
            executablePathTextField.text = selectedFile.toPath().toAbsolutePath().toString()
        }
        val inputBox = HBox(5.0, executablePathTextField, choosePathButton)
        VBox(Label(MusicShelfBundle.get("app_support.executable_based.executable_path")), inputBox)
    }

    override fun createConfigurationNode(): Node {
        executablePath?.let { executablePathTextField.text = it.toString() }
        return configNode
    }

    override fun isChanged(): Boolean {
        return executablePathTextField.text != executablePath.toString()
    }

    override fun applyConfiguration() {
        val newPath = createAndValidatePath(executablePathTextField.text.orEmpty())
        if (newPath != null) {
            executablePath = newPath
        }
    }

    override fun loadConfiguration(configurationNode: ConfigurationNode) {
        val executable = configurationNode.node("executable").string
        if (executable == null) {
            executablePath = null
            return
        }

        executablePath = try {
            createAndValidatePath(executable)
        } catch (e: ConfigurationException) {
            null
        }
    }

    private fun createAndValidatePath(executable: String): Path? {
        try {
            val path = Paths.get(executable)
            if (!Files.isRegularFile(path)) {
                throw ConfigurationException("Executable path does not exists or is not a file: $executable")
            }

            return path
        } catch (e: InvalidPathException) {
            throw ConfigurationException("Invalid executable path: $executable", e)
        }
    }

    override fun saveConfiguration(configurationNode: ConfigurationNode) {
        configurationNode.node("executable").set(executablePath?.toString())
    }
}
