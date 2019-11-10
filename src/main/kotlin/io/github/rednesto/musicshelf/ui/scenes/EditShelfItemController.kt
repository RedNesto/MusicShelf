package io.github.rednesto.musicshelf.ui.scenes

import io.github.rednesto.musicshelf.MusicShelfBundle
import io.github.rednesto.musicshelf.Shelf
import io.github.rednesto.musicshelf.ShelfItem
import java.net.URL
import java.nio.file.Path
import java.util.*

class EditShelfItemController(val originalItem: ShelfItem, shelf: Shelf) :
        CreateShelfItemController(originalItem.path, originalItem.groups, originalItem.info, shelf = shelf) {

    override fun createItem(itemPath: Path, groups: Set<String>, info: Map<String, String>): ShelfItem =
            ShelfItem(originalItem.id, info, groups, itemPath)

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        super.initialize(location, resources)
        createButton.text = resources?.getString("edit.shelf_item.edit") ?: MusicShelfBundle.get("edit.shelf_item.edit")
    }
}
