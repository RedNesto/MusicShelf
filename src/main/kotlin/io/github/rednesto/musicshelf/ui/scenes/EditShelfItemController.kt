package io.github.rednesto.musicshelf.ui.scenes

import io.github.rednesto.musicshelf.MusicShelfBundle
import io.github.rednesto.musicshelf.ShelfItem
import java.net.URL
import java.nio.file.Path
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class EditShelfItemController(val originalItem: ShelfItem) :
        CreateShelfItemController(originalItem.path, originalItem.groups, originalItem.infos, true) {

    override fun createItem(itemPath: Path, groups: List<String>, info: Map<String, String>): ShelfItem =
            ShelfItem(originalItem.id, itemPath, HashMap(info), ArrayList(groups))

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        super.initialize(location, resources)
        createButton.text = resources?.getString("edit.shelf_item.edit") ?: MusicShelfBundle.get("edit.shelf_item.edit")
    }
}
