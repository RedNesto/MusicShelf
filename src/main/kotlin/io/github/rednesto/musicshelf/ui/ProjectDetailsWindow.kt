package io.github.rednesto.musicshelf.ui

import io.github.rednesto.musicshelf.MusicShelfBundle
import io.github.rednesto.musicshelf.Project
import io.github.rednesto.musicshelf.ui.scenes.ProjectDetailsController
import io.github.rednesto.musicshelf.utils.loadFxml
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.stage.Stage

object ProjectDetailsWindow {
    fun create(project: Project): Stage = Stage().apply {
        title = MusicShelfBundle.get("project.details.window_title", project.name)
        scene = Scene(loadFxml<Parent>("/ui/scenes/ProjectDetails.fxml", ProjectDetailsController(project), MusicShelfBundle.getBundle()))
        scene.accelerators[KeyCodeCombination(KeyCode.ESCAPE)] = Runnable { hide() }
    }
}
