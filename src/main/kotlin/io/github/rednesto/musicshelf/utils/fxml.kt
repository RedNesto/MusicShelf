package io.github.rednesto.musicshelf.utils

import io.github.rednesto.musicshelf.MusicShelfApp
import javafx.fxml.FXMLLoader
import java.io.InputStream
import java.util.*

fun <T> loadFxml(location: String, controller: Any? = null, resources: ResourceBundle? = null): T {
    return loadFxml(MusicShelfApp::class.java.getResourceAsStream(location), controller, resources)
}

fun <T> loadFxml(location: InputStream, controller: Any? = null, resources: ResourceBundle? = null): T {
    val loader = FXMLLoader()
    loader.resources = resources
    loader.setController(controller)
    return loader.load(location)
}
