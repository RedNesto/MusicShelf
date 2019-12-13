package io.github.rednesto.musicshelf.ui

import io.github.rednesto.musicshelf.MusicShelfBundle
import io.github.rednesto.musicshelf.Project
import io.github.rednesto.musicshelf.ui.scenes.ProjectDetailsController
import io.github.rednesto.musicshelf.utils.DesktopHelper
import javafx.scene.Scene
import javafx.scene.control.ListView
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseButton
import javafx.scene.text.Text
import javafx.stage.Modality
import javafx.stage.Stage
import java.nio.file.Path

object ProjectFilesViewDialog {
    fun showAndWait(project: Project) {
        val filesView = ListView<Pair<String, Path>>().apply {
            prefWidth = 400.0
            placeholder = Text(MusicShelfBundle.get("project.files.list_placeholder"))
            setOnKeyPressed { event ->
                if (event.code == KeyCode.ENTER) {
                    selectionModel.selectedItems.forEach { item ->
                        DesktopHelper.open(item.second)
                    }
                    event.consume()
                }
            }
            setOnMouseClicked { event ->
                if (event.clickCount >= 2 && event.button == MouseButton.PRIMARY) {
                    selectionModel.selectedItem?.second?.let(DesktopHelper::open)
                    event.consume()
                }
            }
            setCellFactory { ProjectDetailsController.FileListCell() }
        }
        filesView.items.addAll(project.filesCollector.collect().toList())

        val stage = Stage().apply {
            title = MusicShelfBundle.get("project.files.view.title", project.name)
            scene = Scene(filesView)
            initModality(Modality.APPLICATION_MODAL)
        }
        stage.showAndWait()
    }
}
