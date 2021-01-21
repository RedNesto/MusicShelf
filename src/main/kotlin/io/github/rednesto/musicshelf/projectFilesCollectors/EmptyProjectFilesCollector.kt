package io.github.rednesto.musicshelf.projectFilesCollectors

import io.github.rednesto.musicshelf.ProjectFilesCollector
import javafx.scene.Node
import javafx.scene.text.Text
import org.spongepowered.configurate.ConfigurationNode
import java.nio.file.Path
import java.util.*

object EmptyProjectFilesCollector : ProjectFilesCollector {
    override val id: String = "empty"

    override fun getDisplayname(locale: Locale): String = "<Empty>"

    override fun createConfigurationNode(): Node = Text("<Empty>")

    override fun applyConfiguration() = Unit

    override fun loadConfiguration(configurationNode: ConfigurationNode) = Unit

    override fun saveConfiguration(configurationNode: ConfigurationNode) = Unit

    override fun collect(): Map<String, Path> = emptyMap()
}
