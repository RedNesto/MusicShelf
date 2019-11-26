package io.github.rednesto.musicshelf.utils

import io.github.rednesto.musicshelf.ProjectFilesCollector
import ninja.leaping.configurate.SimpleConfigurationNode

fun ProjectFilesCollector.transferConfigWithFallback(other: ProjectFilesCollector) {
    if (!transferConfigurationTo(other)) {
        val initialCollectorConfig = SimpleConfigurationNode.root()
        this.saveConfiguration(initialCollectorConfig)
        other.loadConfiguration(initialCollectorConfig)
    }
}
