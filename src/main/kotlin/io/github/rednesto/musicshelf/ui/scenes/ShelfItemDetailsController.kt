package io.github.rednesto.musicshelf.ui.scenes

import io.github.rednesto.musicshelf.ShelfItem
import io.github.rednesto.musicshelf.utils.DesktopHelper
import javafx.beans.property.ReadOnlyStringWrapper
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import javafx.scene.text.Text
import java.net.URL
import java.util.*

class ShelfItemDetailsController(val item: ShelfItem) : Initializable {

    @FXML
    lateinit var nameText: Text

    @FXML
    lateinit var filePathTextField: TextField

    @FXML
    lateinit var infoTableView: TableView<Pair<String, String>>

    @FXML
    lateinit var infoKeyColumn: TableColumn<Pair<String, String>, String>

    @FXML
    lateinit var infoValueColumn: TableColumn<Pair<String, String>, String>

    @FXML
    fun openFileButton_onAction(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
        DesktopHelper.open(item.path)
    }

    @FXML
    lateinit var showInExplorerButton: Button

    @FXML
    fun showInExplorerButton_onAction(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
        DesktopHelper.show(item.path)
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        nameText.text = item.name
        filePathTextField.text = item.path.toAbsolutePath().toString()

        infoKeyColumn.setCellValueFactory { ReadOnlyStringWrapper(it.value.first) }
        infoValueColumn.setCellValueFactory { ReadOnlyStringWrapper(it.value.second) }

        item.info.forEach { infoTableView.items.add(it.toPair()) }

        showInExplorerButton.isDisable = !DesktopHelper.supportsShow()
    }
}
