package io.github.rednesto.musicshelf.ui

import io.github.rednesto.musicshelf.MusicShelfBundle
import io.github.rednesto.musicshelf.Project
import io.github.rednesto.musicshelf.Shelf
import io.github.rednesto.musicshelf.ui.scenes.CreateProjectController
import io.github.rednesto.musicshelf.utils.configureFxmlLoader
import javafx.scene.Scene
import javafx.stage.Modality
import javafx.stage.Stage

object CreateProjectDialog {
    fun showAndGet(controller: CreateProjectController? = null): Project? {
        val loader = configureFxmlLoader("/ui/scenes/CreateProject.fxml", resources = MusicShelfBundle.getBundle())
        if (controller != null) {
            loader.setControllerFactory { controller }
        }
        val stage = Stage().apply {
            scene = Scene(loader.load())
            title = MusicShelfBundle.get("create.project.window_title")
            initModality(Modality.APPLICATION_MODAL)
        }
        stage.showAndWait()
        return loader.getController<CreateProjectController>()?.result
    }

    fun showAndUpdateShelf(shelf: Shelf, controller: CreateProjectController = CreateProjectController(shelf = shelf)) {
        showAndGet(controller)?.let(shelf::addProject)
    }
}
