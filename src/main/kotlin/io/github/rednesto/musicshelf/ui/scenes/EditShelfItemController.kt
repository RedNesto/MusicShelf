package io.github.rednesto.musicshelf.ui.scenes

import io.github.rednesto.musicshelf.ShelfItem
import java.nio.file.Path

class EditShelfItemController(val originalItem: ShelfItem) :
        CreateShelfItemController(originalItem.path, originalItem.groups, originalItem.infos, true) {

    override val createButtonTextKey: String
        get() = "edit.shelf_item.edit"

    override fun createItem(itemPath: Path, groups: List<String>, info: Map<String, String>): ShelfItem =
            ShelfItem(originalItem.id, itemPath, HashMap(info), ArrayList(groups))
}
