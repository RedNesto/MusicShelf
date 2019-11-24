package io.github.rednesto.musicshelf.ui.scenes

import io.github.rednesto.musicshelf.MusicShelfBundle
import io.github.rednesto.musicshelf.Project
import io.github.rednesto.musicshelf.ProjectFilesCollector
import io.github.rednesto.musicshelf.Shelf
import java.net.URL
import java.util.*

class EditProjectController(val originalProject: Project, shelf: Shelf) :
        CreateProjectController(originalProject.name, originalProject.groups, originalProject.info, originalProject.filesCollector, shelf) {

    override fun createItem(name: String, groups: Set<String>, info: Map<String, String>, filesCollector: ProjectFilesCollector): Project =
            Project(originalProject.id, name, groups, info, filesCollector)

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        super.initialize(location, resources)
        createButton.text = resources?.getString("edit.project.edit") ?: MusicShelfBundle.get("edit.project.edit")
    }
}
