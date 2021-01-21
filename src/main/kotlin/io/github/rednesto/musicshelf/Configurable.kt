package io.github.rednesto.musicshelf

import javafx.scene.Node
import org.spongepowered.configurate.ConfigurationNode

/**
 * A general purpose interface used for any object that can be configured via the GUI.
 */
interface Configurable {
    /**
     * Creates a node containing controls used to configure this instance.
     *
     * Changes should not be applied on any change in this node,
     * but rather when [applyConfiguration] is called.
     */
    fun createConfigurationNode(): Node

    /**
     * Checks whether this configurable's inputs have different values than
     * the original ones, this means calling [applyConfiguration] will change
     * this instance in some way.
     *
     * Most implementations simply will compare the
     * [configuration node][createConfigurationNode] inputs and currently used
     * values for any differences, and return `true` if at least one was found.
     */
    fun isChanged(): Boolean

    /**
     * Applies values from its [configuration node][createConfigurationNode].
     *
     * Updated values should also be the ones to be [saved][saveConfiguration].
     *
     * @throws ConfigurationException if the configuration could not be applied
     * for some reason, i.e. some inputs are invalid.
     */
    @Throws(ConfigurationException::class)
    fun applyConfiguration()

    /**
     * Loads configuration from the [configurationNode]
     */
    fun loadConfiguration(configurationNode: ConfigurationNode)

    /**
     * Saves this configuration to the [configurationNode].
     */
    fun saveConfiguration(configurationNode: ConfigurationNode)
}

class ConfigurationException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)
