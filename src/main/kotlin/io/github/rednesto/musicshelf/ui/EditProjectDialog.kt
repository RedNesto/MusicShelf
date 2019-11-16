package io.github.rednesto.musicshelf.ui

import io.github.rednesto.musicshelf.MusicShelfBundle
import io.github.rednesto.musicshelf.Project
import io.github.rednesto.musicshelf.Shelf
import io.github.rednesto.musicshelf.ui.scenes.EditProjectController
import io.github.rednesto.musicshelf.utils.configureFxmlLoader
import javafx.scene.Scene
import javafx.stage.Modality
import javafx.stage.Stage

object EditProjectDialog {
    fun showAndGet(project: Project, shelf: Shelf): Project? {
        val loader = configureFxmlLoader("/ui/scenes/CreateProject.fxml", resources = MusicShelfBundle.getBundle())
        loader.setControllerFactory { EditProjectController(project, shelf) }
        val stage = Stage().apply {
            scene = Scene(loader.load())
            title = MusicShelfBundle.get("edit.project.window_title")
            initModality(Modality.APPLICATION_MODAL)
        }
        stage.showAndWait()
        return loader.getController<EditProjectController>()?.result
    }

    fun showAndUpdateShelf(project: Project, shelf: Shelf) {
        showAndGet(project, shelf)?.let(shelf::addProject)
    }
}
