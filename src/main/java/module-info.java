module musicshelf {
    requires kotlin.stdlib;
    requires kotlin.stdlib.jdk8;

    requires java.desktop;

    requires javafx.controls;
    requires javafx.fxml;

    requires configurate.core;
    requires configurate.xml;

    requires guava;

    requires org.jetbrains.annotations;

    exports io.github.rednesto.musicshelf;
    exports io.github.rednesto.musicshelf.ui;
    exports io.github.rednesto.musicshelf.ui.scenes;
}
