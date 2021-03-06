package io.github.rednesto.musicshelf.appSupport

import io.github.rednesto.musicshelf.Configurable
import org.spongepowered.configurate.BasicConfigurationNode
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.hocon.HoconConfigurationLoader
import java.lang.module.ModuleFinder
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

object AppSupportManager {

    const val DEFAULT_FILE_NAME = "appSupport.conf"

    var fileApps: Map<String, FileAppSupport> = emptyMap()
        private set

    fun load(filePath: Path) {
        val rootNode = if (Files.isRegularFile(filePath)) {
            HoconConfigurationLoader.builder().path(filePath).build().load()
        } else {
            BasicConfigurationNode.root()
        }
        load(rootNode)
    }

    fun load(rootNode: ConfigurationNode) {
        fileApps = loadAppSupports(createLoader(), rootNode.node("fileApps"))
    }

    private fun createLoader(): ServiceLoader<FileAppSupport> {
        val pluginsDir = Paths.get("plugins")
        println("Plugin dir: ${pluginsDir.toAbsolutePath()}")
        val finder = ModuleFinder.of(pluginsDir)
        val allRoots = finder.findAll().mapTo(mutableSetOf()) { it.descriptor().name() }
        val parent = ModuleLayer.boot().configuration().resolve(finder, ModuleFinder.of(), allRoots)
        val moduleLayer = ModuleLayer.boot().defineModulesWithManyLoaders(parent, javaClass.classLoader)
        return ServiceLoader.load(moduleLayer, FileAppSupport::class.java)
    }

    private fun <S : AppSupport> loadAppSupports(serviceLoader: ServiceLoader<S>, rootNode: ConfigurationNode): Map<String, S> {
        serviceLoader.iterator().forEachRemaining {
            println(it.javaClass.name)
            println(it.javaClass.classLoader)
            println(it.javaClass.classLoader.name)
        }
        return serviceLoader
                .onEach {
                    try {
                        (it as? Configurable)?.loadConfiguration(rootNode.node(it.id))
                    } catch (t: Throwable) {
                        println("Could not load configuration of AppSupport ${it.id}")
                        println(t)
                    }
                }
                .associateBy { it.id }
    }


    fun save(filePath: Path) {
        val loader = HoconConfigurationLoader.builder().path(filePath).build()
        val rootNode = loader.createNode()
        save(rootNode)
        loader.save(rootNode)
    }

    private fun save(rootNode: CommentedConfigurationNode) {
        saveAppSupports(fileApps, rootNode.node("fileApps"))
    }

    private fun saveAppSupports(supports: Map<String, FileAppSupport>, sheetMusicEditorsNode: CommentedConfigurationNode) {
        supports.forEach {
            try {
                (it.value as? Configurable)?.saveConfiguration(sheetMusicEditorsNode.node(it.key))
            } catch (t: Throwable) {
                println("Could not save configuration of AppSupport ${it.value.id}")
                println(t)
            }
        }
    }
}
