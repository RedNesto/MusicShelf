package io.github.rednesto.musicshelf.ui.scenes;

import io.github.rednesto.musicshelf.*
import io.github.rednesto.musicshelf.projectFilesCollectors.EmptyProjectFilesCollector
import io.github.rednesto.musicshelf.ui.ShelvableGroupsListViewHelper
import io.github.rednesto.musicshelf.ui.ShelvableInfoTableViewHelper
import io.github.rednesto.musicshelf.utils.isRootGroup
import io.github.rednesto.musicshelf.utils.normalizeGroups
import io.github.rednesto.musicshelf.utils.renameToAvoidDuplicates
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.VBox
import java.net.URL
import java.util.*

open class CreateProjectController @JvmOverloads constructor(
        val initialName: String? = null,
        val initialGroups: Set<String> = emptySet(),
        val initialInfo: Map<String, String> = ShelfItemInfoKeys.DEFAULT_VALUES,
        val initialFilesCollector: ProjectFilesCollector = EmptyProjectFilesCollector,
        val shelf: Shelf? = null
) : Initializable {

    var result: Project? = null
        private set

    @FXML
    lateinit var nameTextField: TextField

    @FXML
    lateinit var itemInfoTableView: TableView<Pair<String, String>>

    @FXML
    fun itemInfoTableView_onKeyPressed(event: KeyEvent) {
        if (event.code == KeyCode.DELETE) {
            itemInfoTableView.items.removeAll(itemInfoTableView.selectionModel.selectedItems)
        }
    }

    @FXML
    lateinit var itemInfoLabel: Label

    @FXML
    lateinit var infoKeyColumn: TableColumn<Pair<String, String>, String>

    @FXML
    lateinit var infoValueColumn: TableColumn<Pair<String, String>, String>

    @FXML
    fun addInfoButton_onAction(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
        val key = renameToAvoidDuplicates(MusicShelfBundle.get("create.shelf_item.info.default_key"), itemInfoTableView.items.map { it.first })
        val value = MusicShelfBundle.get("create.shelf_item.info.default_value")
        itemInfoTableView.items.add(key to value)
    }

    @FXML
    fun removeInfoButton_onAction(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
        itemInfoTableView.items.removeAll(itemInfoTableView.selectionModel.selectedItems)
    }

    @FXML
    lateinit var itemGroupsLabel: Label

    @FXML
    lateinit var itemGroupsListView: ListView<String>

    @FXML
    fun itemGroupsListView_onKeyPressed(event: KeyEvent) {
        if (event.code == KeyCode.DELETE) {
            itemGroupsListView.items.removeAll(itemGroupsListView.selectionModel.selectedItems)
        }
    }

    @FXML
    lateinit var addToRootCheckbox: CheckBox

    @FXML
    fun addGroupButton_onAction(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
        val groupName = renameToAvoidDuplicates(MusicShelfBundle.get("create.shelf_item.group.default_name"), itemGroupsListView.items)
        itemGroupsListView.items.add(groupName)
        itemGroupsListView.scrollTo(groupName)
        itemGroupsListView.requestFocus()
        itemGroupsListView.selectionModel.clearAndSelect(itemGroupsListView.items.lastIndex)
    }

    @FXML
    fun removeGroupButton_onAction(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
        itemGroupsListView.items.removeAll(itemGroupsListView.selectionModel.selectedItems)
    }

    val filesCollectorsToggleGroup: ToggleGroup = ToggleGroup()

    private lateinit var availableFilesCollectors: Map<String, ProjectFilesCollector>

    var selectedFilesCollector: ProjectFilesCollector? = null

    @FXML
    lateinit var filesCollectorsVBox: VBox

    @FXML
    lateinit var filesCollectorConfigPane: VBox

    @FXML
    lateinit var createButton: Button

    @FXML
    fun createButton_onAction(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
        val name = nameTextField.text
        if (name.isNullOrBlank()) {
            Alert(Alert.AlertType.ERROR, MusicShelfBundle.get("create.shelf_item.error.empty_name"), ButtonType.OK).apply {
                title = MusicShelfBundle.get("create.shelf_item.error.empty_name.title")
                showAndWait()
            }
            return
        }

        val groups = mutableSetOf<String>()
        if (itemGroupsListView.items.isNotEmpty()) {
            groups.addAll(normalizeGroups(itemGroupsListView.items))
        }
        if (addToRootCheckbox.isSelected) {
            groups.add("/")
        }

        val filesCollector = filesCollectorsToggleGroup.selectedToggle.userData
                as? ProjectFilesCollector ?: EmptyProjectFilesCollector
        filesCollector.applyConfiguration()

        result = createItem(name, groups, itemInfoTableView.items.toMap(), filesCollector)

        nameTextField.scene.window.hide()
    }

    protected open fun createItem(name: String, groups: Set<String>, info: Map<String, String>, filesCollector: ProjectFilesCollector) =
            Project(UUID.randomUUID(), name, groups, info, filesCollector)

    @FXML
    fun cancelButton_onAction(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
        nameTextField.scene.window.hide()
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        itemInfoLabel.labelFor = itemInfoTableView
        ShelvableInfoTableViewHelper.configure(itemInfoTableView, infoKeyColumn, infoValueColumn)

        itemInfoTableView.items.addAll(initialInfo.toList())

        itemGroupsLabel.labelFor = itemGroupsListView
        ShelvableGroupsListViewHelper.configure(itemGroupsListView, addToRootCheckbox, shelf)

        filesCollectorsToggleGroup.selectedToggleProperty().addListener { _, _, newValue ->
            val collector = newValue.userData as? ProjectFilesCollector ?: return@addListener
            filesCollectorConfigPane.alignment = Pos.TOP_CENTER
            filesCollectorConfigPane.children[0] = collector.createConfigurationNode()
        }

        val mutableFilesCollectors = ProjectFilesCollectorsLoader.createAllCollectors()
                .associateByTo(mutableMapOf(), ProjectFilesCollector::id)
        availableFilesCollectors = mutableFilesCollectors

        val isEmptyInitialCollector = initialFilesCollector !is EmptyProjectFilesCollector
        if (isEmptyInitialCollector) {
            mutableFilesCollectors[initialFilesCollector.id] = initialFilesCollector
            selectedFilesCollector = initialFilesCollector
        }

        val localeToUse = resources?.let(ResourceBundle::getLocale) ?: Locale.getDefault()
        filesCollectorsVBox.children.addAll(availableFilesCollectors.values.map { collector ->
            RadioButton(collector.getDisplayname(localeToUse)).apply {
                toggleGroup = filesCollectorsToggleGroup
                userData = collector
                if (isEmptyInitialCollector && initialFilesCollector.id == collector.id) {
                    isSelected = true
                    style = "-fx-font-weight: bold"
                }
            }
        })

        if (initialName != null && initialName.isNotBlank()) {
            nameTextField.text = initialName
        }

        val sanitizedInitialGroups = initialGroups.toMutableSet()
        addToRootCheckbox.isSelected = sanitizedInitialGroups.removeAll { isRootGroup(it) }
        itemGroupsListView.items.addAll(sanitizedInitialGroups)
        addToRootCheckbox.isDisable = itemGroupsListView.items.isEmpty()
    }
}
