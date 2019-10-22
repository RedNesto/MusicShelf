package io.github.rednesto.musicshelf.utils

import io.github.rednesto.musicshelf.MusicShelfApp
import javafx.fxml.FXMLLoader
import java.net.URL
import java.util.*

fun <T> loadFxml(location: String, controller: Any? = null, resources: ResourceBundle? = null): T {
    val locationUrl = MusicShelfApp::class.java.getResource(location)
            ?: throw NullPointerException("Could not find fxml file at $location")
    return loadFxml(locationUrl, controller, resources)
}

fun <T> loadFxml(location: URL, controller: Any? = null, resources: ResourceBundle? = null): T {
    val loader = FXMLLoader(location)
    loader.resources = resources
    loader.setController(controller)
    return loader.load()
}
