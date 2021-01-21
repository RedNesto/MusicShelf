package io.github.rednesto.musicshelf.utils

import io.github.rednesto.musicshelf.ProjectFilesCollector
import org.spongepowered.configurate.BasicConfigurationNode

fun ProjectFilesCollector.transferConfigWithFallback(other: ProjectFilesCollector) {
    if (!transferConfigurationTo(other)) {
        val initialCollectorConfig = BasicConfigurationNode.root()
        this.saveConfiguration(initialCollectorConfig)
        other.loadConfiguration(initialCollectorConfig)
    }
}
