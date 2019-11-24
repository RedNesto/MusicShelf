package io.github.rednesto.musicshelf

import java.util.*

object ProjectFilesCollectorsLoader {

    fun createAllCollectors(): Collection<ProjectFilesCollector> =
            ServiceLoader.load(ProjectFilesCollector::class.java).toList()

    fun getCollector(id: String): ProjectFilesCollector? =
            createAllCollectors().find { it.id == id }
}
