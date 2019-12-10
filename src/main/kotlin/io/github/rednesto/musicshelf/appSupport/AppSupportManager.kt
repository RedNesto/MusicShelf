package io.github.rednesto.musicshelf.appSupport

import io.github.rednesto.musicshelf.Configurable
import ninja.leaping.configurate.ConfigurationNode
import ninja.leaping.configurate.commented.CommentedConfigurationNode
import ninja.leaping.configurate.hocon.HoconConfigurationLoader
import java.nio.file.Path
import java.util.*

object AppSupportManager {

    const val DEFAULT_FILE_NAME = "appSupport.conf"

    var fileApps: Map<String, FileAppSupport> = emptyMap()
        private set


    fun load(filePath: Path) {
        val loader = HoconConfigurationLoader.builder().setPath(filePath).build()
        val rootNode = loader.load()
        load(rootNode)
    }

    fun load(rootNode: ConfigurationNode) {
        fileApps = loadAppSupports(ServiceLoader.load(FileAppSupport::class.java), rootNode.getNode("fileApps"))
    }

    private fun <S : AppSupport> loadAppSupports(serviceLoader: ServiceLoader<S>, rootNode: ConfigurationNode): Map<String, S> {
        return serviceLoader
                .onEach {
                    try {
                        (it as? Configurable)?.loadConfiguration(rootNode.getNode(it.id))
                    } catch (t: Throwable) {
                        println("Could not load configuration of AppSupport ${it.id}")
                        println(t)
                    }
                }
                .map { it.id to it }
                .toMap()
    }


    fun save(filePath: Path) {
        val loader = HoconConfigurationLoader.builder().setPath(filePath).build()
        val rootNode = loader.createEmptyNode()
        save(rootNode)
        loader.save(rootNode)
    }

    private fun save(rootNode: CommentedConfigurationNode) {
        saveAppSupports(fileApps, rootNode.getNode("fileApps"))
    }

    private fun saveAppSupports(supports: Map<String, FileAppSupport>, sheetMusicEditorsNode: CommentedConfigurationNode) {
        supports.forEach {
            try {
                (it.value as? Configurable)?.saveConfiguration(sheetMusicEditorsNode.getNode(it.key))
            } catch (t: Throwable) {
                println("Could not save configuration of AppSupport ${it.value.id}")
                println(t)
            }
        }
    }
}
