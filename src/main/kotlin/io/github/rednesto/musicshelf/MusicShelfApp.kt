package io.github.rednesto.musicshelf

import io.github.rednesto.musicshelf.appSupport.AppSupportManager
import io.github.rednesto.musicshelf.ui.ShelfViewWindow
import javafx.application.Application
import javafx.stage.Stage
import java.nio.file.Files
import java.nio.file.Paths

class MusicShelfApp : Application() {

    override fun init() {
        app = this

        val appSupportFile = Paths.get(AppSupportManager.DEFAULT_FILE_NAME)
        if (Files.exists(appSupportFile)) {
            AppSupportManager.load(appSupportFile)
        }

        // TODO support multiple shelves
        val mainShelfPath = Paths.get("").toAbsolutePath()
        mainShelf = Shelf("Main Shelf", mainShelfPath).apply {
            load()
            this.addItemChangeListener(object : Shelf.SimpleChangeListener<ShelfItem> {
                override fun onItemChange(oldItem: ShelfItem?, newItem: ShelfItem?) {
                    ShelfItemInfoIndex.clear()
                    ShelfItemInfoIndex.indexShelf(this@apply)
                }
            })
            this.addProjectChangeListener(object : Shelf.SimpleChangeListener<Project> {
                override fun onItemChange(oldItem: Project?, newItem: Project?) {
                    ShelfItemInfoIndex.clear()
                    ShelfItemInfoIndex.indexShelf(this@apply)
                }
            })
            ShelfItemInfoIndex.indexShelf(this)
        }
    }

    override fun start(primaryStage: Stage) {
        ShelfViewWindow.create(mainShelf, primaryStage).show()
    }

    override fun stop() {
        mainShelf.save()
        AppSupportManager.save(Paths.get(AppSupportManager.DEFAULT_FILE_NAME))
    }

    companion object {
        lateinit var app: MusicShelfApp
            private set

        lateinit var mainShelf: Shelf
            private set

        @JvmStatic
        fun main(args: Array<String>) {
            launch(MusicShelfApp::class.java, *args)
        }
    }
}
