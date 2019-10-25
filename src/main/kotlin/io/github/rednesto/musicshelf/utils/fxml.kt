package io.github.rednesto.musicshelf.utils

import io.github.rednesto.musicshelf.MusicShelfApp
import javafx.fxml.FXMLLoader
import java.net.URL
import java.util.*

fun <T> loadFxml(location: String, controller: Any? = null, resources: ResourceBundle? = null): T {
    return configureFxmlLoader(location, controller, resources).load()
}

fun configureFxmlLoader(location: String, controller: Any? = null, resources: ResourceBundle? = null): FXMLLoader {
    val locationUrl = MusicShelfApp::class.java.getResource(location)
            ?: throw NullPointerException("Could not find fxml file at $location")
    return configureFxmlLoader(locationUrl, controller, resources)
}

fun configureFxmlLoader(location: URL, controller: Any? = null, resources: ResourceBundle? = null): FXMLLoader {
    val loader = FXMLLoader(location)
    loader.resources = resources
    loader.setController(controller)
    return loader
}
