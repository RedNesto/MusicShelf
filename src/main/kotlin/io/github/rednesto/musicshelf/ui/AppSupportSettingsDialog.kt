package io.github.rednesto.musicshelf.ui

import io.github.rednesto.musicshelf.Configurable
import io.github.rednesto.musicshelf.ConfigurationException
import io.github.rednesto.musicshelf.MusicShelfBundle
import io.github.rednesto.musicshelf.appSupport.AppSupport
import io.github.rednesto.musicshelf.appSupport.AppSupportManager
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.BorderPane
import javafx.scene.text.Text
import javafx.stage.Modality
import javafx.stage.Stage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch

object AppSupportSettingsDialog {
    fun show() {
        val appConfigPane = BorderPane(Text(MusicShelfBundle.get("settings.app_support.configuration.placeholder")))
        val appsListView = ListView<AppSupport>()
        val applyButton = Button(MusicShelfBundle.get("settings.app_support.configuration.apply"))
        val revertButton = Button(MusicShelfBundle.get("settings.app_support.configuration.revert"))

        fun refreshChangedState() {
            val selectedSupport = appsListView.selectionModel.selectedItem
            if (selectedSupport is Configurable) {
                val didNotChange = !selectedSupport.isChanged()
                revertButton.isDisable = didNotChange
                applyButton.isDisable = didNotChange
            }
        }

        with(appConfigPane) {
            padding = Insets(5.0)
        }
        with(appsListView) {
            setCellFactory { AppSupportListCell() }
            selectionModel.selectedItemProperty().addListener { _, _, newValue ->
                val configContent = if (newValue is Configurable) {
                    newValue.createConfigurationNode()
                } else {
                    Text(MusicShelfBundle.get("settings.app_support.not_configurable"))
                }
                appConfigPane.center = null // Remove the initial placeholder
                appConfigPane.top = configContent
                refreshChangedState()
            }
            items.addAll(AppSupportManager.fileApps.values)
        }
        val configButtonBar = ButtonBar().apply {
            with (revertButton) {
                isDisable = true
                setOnAction {
                    val selectedSupport = appsListView.selectionModel.selectedItem
                    if (selectedSupport is Configurable) {
                        appConfigPane.top = selectedSupport.createConfigurationNode()
                        refreshChangedState()
                    }
                }
                ButtonBar.setButtonData(this, ButtonBar.ButtonData.CANCEL_CLOSE)
            }
            with(applyButton) {
                isDisable = true
                setOnAction {
                    val selectedSupport = appsListView.selectionModel.selectedItem
                    if (selectedSupport is Configurable) {
                        try {
                            selectedSupport.applyConfiguration()
                            refreshChangedState()
                        } catch (e: ConfigurationException) {
                            Alert(Alert.AlertType.ERROR, "Could not apply configuration: ${e.message}", ButtonType.OK).showAndWait()
                        }
                    }
                }
                ButtonBar.setButtonData(this, ButtonBar.ButtonData.APPLY)
            }
            buttons.addAll(revertButton, applyButton)
        }
        appConfigPane.bottom = configButtonBar
        val root = SplitPane(appsListView, appConfigPane).apply {
            setDividerPosition(0, 0.20)
        }
        val stage = Stage().apply {
            scene = Scene(root, 700.0, 450.0)
            title = MusicShelfBundle.get("settings.app_support.title")
            initModality(Modality.APPLICATION_MODAL)
        }

        GlobalScope.launch(Dispatchers.JavaFx) {
            while (stage.isShowing) {
                delay(2000)
                refreshChangedState()
            }
        }

        stage.showAndWait()
    }
}

private class AppSupportListCell : ListCell<AppSupport>() {
    override fun updateItem(item: AppSupport?, empty: Boolean) {
        super.updateItem(item, empty)

        if (item == null || empty) {
            text = null
            graphic = null
        } else {
            text = item.getDisplayname(MusicShelfBundle.getBundle().locale)
        }
    }
}
