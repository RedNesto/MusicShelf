package io.github.rednesto.musicshelf.ui.scenes

import io.github.rednesto.musicshelf.*
import io.github.rednesto.musicshelf.projectFilesCollectors.EmptyProjectFilesCollector
import io.github.rednesto.musicshelf.ui.ProjectFilesTableViewHelper
import io.github.rednesto.musicshelf.ui.ShelvableGroupsListViewHelper
import io.github.rednesto.musicshelf.ui.ShelvableInfoTableViewHelper
import io.github.rednesto.musicshelf.utils.*
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.VBox
import javafx.stage.Stage
import java.net.URL
import java.nio.file.Path
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
    lateinit var filesPreviewButton: Button

    val filesPreviewTableView: TableView<Pair<String, Path>> by lazy {
        TableView<Pair<String, Path>>().apply {
            val nameColumn = TableColumn<Pair<String, Path>, String>(MusicShelfBundle.get("project.files_collectors.listed_files.table.header.name"))
            val pathColumn = TableColumn<Pair<String, Path>, Path>(MusicShelfBundle.get("project.files_collectors.listed_files.table.header.path"))
            columns.addAll(nameColumn, pathColumn)
            ProjectFilesTableViewHelper.configure(this, nameColumn, pathColumn)
        }
    }
    val filesPreviewWindow: Stage by lazy {
        Stage().apply {
            title = MusicShelfBundle.get("create.project.files_collectors.preview.title")
            scene = Scene(filesPreviewTableView)
            initOwner(filesPreviewButton.scene.window)
            setOnHidden {
                filesPreviewButton.text = MusicShelfBundle.get("create.project.files_collectors.preview.show")
            }
        }
    }

    fun refreshFilesPreview() {
        filesPreviewTableView.items.clear()
        selectedFilesCollector?.let {
            it.applyConfiguration()
            filesPreviewTableView.items.addAll(it.collect().toList())
        }
    }

    @FXML
    fun filesPreviewButton_onAction(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
        refreshFilesPreview()
        if (!filesPreviewWindow.isShowing) {
            filesPreviewWindow.show()

            // We show the window before resizing columns because we need the
            // TableView's skin, which is initialized the first time it is shown
            tryResizeColumnsToContent(filesPreviewTableView)
            filesPreviewWindow.width = getWidthOfAllColumns(filesPreviewTableView) + 35.0

            filesPreviewButton.text = MusicShelfBundle.get("create.project.files_collectors.preview.update")
        }
    }

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

        val filesCollector = filesCollectorsToggleGroup.selectedToggle?.userData
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
            selectedFilesCollector = collector
        }

        val mutableFilesCollectors = ProjectFilesCollectorsLoader.createAllCollectors()
                .associateByTo(mutableMapOf(), ProjectFilesCollector::id)
        availableFilesCollectors = mutableFilesCollectors

        val isEmptyInitialCollector = initialFilesCollector !is EmptyProjectFilesCollector
        val localeToUse = resources?.let(ResourceBundle::getLocale) ?: Locale.getDefault()
        filesCollectorsVBox.children.addAll(1, availableFilesCollectors.values.map { collector ->
            RadioButton(collector.getDisplayname(localeToUse)).apply {
                toggleGroup = filesCollectorsToggleGroup
                userData = collector
                if (isEmptyInitialCollector && initialFilesCollector.id == collector.id) {
                    try {
                        initialFilesCollector.transferConfigWithFallback(collector)
                    } catch (e: Throwable) {
                        println("Failed to transfer configuration from $initialFilesCollector to $collector")
                        println(e)
                    }

                    mutableFilesCollectors[collector.id] = collector
                    selectedFilesCollector = collector

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
