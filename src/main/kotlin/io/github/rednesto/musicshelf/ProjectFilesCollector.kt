package io.github.rednesto.musicshelf

import javafx.scene.Node
import org.spongepowered.configurate.ConfigurationNode
import java.nio.file.Path
import java.util.*

/**
 * A ProjectFilesCollector role is to collect files for a specific [Project]
 * based on its own strategy, and optionally its configuration.
 */
interface ProjectFilesCollector {

    /**
     * The unique identifier of this collector.
     */
    val id: String

    /**
     * Returns the name of this collector to display to the user,
     * optionally translated for the provided [locale].
     *
     * @param locale the locale currently used by the application.
     */
    fun getDisplayname(locale: Locale): String

    /**
     * Creates the node to configure this collector instance.
     *
     * It will be displayed in CreateProjectDialog and EditProjectDialog
     * if this collector has been selected.
     *
     * The returned node can be cached by this collector because collector
     * instances are not reused for different projects.
     */
    fun createConfigurationNode(): Node

    /**
     * Called when this instance's configuration should be applied and used for
     * future calls to [collect].
     *
     * This method is called after creating or editing a project via the GUI,
     * this means changed values are likely to be accessible from the controls
     * contained in the [configuration UI node][createConfigurationNode].
     *
     * Updated values should also be the ones to be [saved][saveConfiguration].
     */
    fun applyConfiguration()

    /**
     * Loads the configuration of this collector from the [configurationNode]
     */
    fun loadConfiguration(configurationNode: ConfigurationNode)

    /**
     * Saves the configuration of this collector to the [configurationNode].
     */
    fun saveConfiguration(configurationNode: ConfigurationNode)

    /**
     * Transfers this collector's configuration to the [other] one.
     *
     * @return `true` if the transfer was successful, `false` if the transfer
     * could not be done or is not supported
     */
    fun transferConfigurationTo(other: ProjectFilesCollector): Boolean = false

    /**
     * Collects all the files based on this collector's strategy, and optionally
     * its configuration.
     */
    fun collect(): Map<String, Path>
}
