import io.github.rednesto.musicshelf.ProjectFilesCollector;

module musicshelf {
    requires kotlin.stdlib;
    requires kotlin.stdlib.jdk7;
    requires kotlin.stdlib.jdk8;

    requires kotlinx.coroutines.core;
    requires kotlinx.coroutines.javafx;

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

    uses ProjectFilesCollector;

    provides ProjectFilesCollector with
            io.github.rednesto.musicshelf.projectFilesCollectors.ListedProjectFilesCollector,
            io.github.rednesto.musicshelf.projectFilesCollectors.DirectoryProjectFilesCollector;
}
