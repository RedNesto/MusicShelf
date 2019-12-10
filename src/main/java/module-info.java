import io.github.rednesto.musicshelf.ProjectFilesCollector;
import io.github.rednesto.musicshelf.appSupport.FileAppSupport;

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
    requires configurate.hocon;
    requires configurate.xml;

    requires guava;

    requires org.jetbrains.annotations;

    exports io.github.rednesto.musicshelf;
    exports io.github.rednesto.musicshelf.projectFilesCollectors;
    exports io.github.rednesto.musicshelf.ui;
    exports io.github.rednesto.musicshelf.ui.scenes;

    uses ProjectFilesCollector;

    provides ProjectFilesCollector with
            io.github.rednesto.musicshelf.projectFilesCollectors.ListedProjectFilesCollector,
            io.github.rednesto.musicshelf.projectFilesCollectors.DirectoryProjectFilesCollector;

    uses FileAppSupport;

    provides FileAppSupport with
            io.github.rednesto.musicshelf.appSupport.builtin.AcrobatReaderAppSupport,
            io.github.rednesto.musicshelf.appSupport.builtin.Musescore2AppSupport,
            io.github.rednesto.musicshelf.appSupport.builtin.Musescore3AppSupport;
}
