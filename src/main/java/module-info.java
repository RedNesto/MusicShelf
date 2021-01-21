import io.github.rednesto.musicshelf.ProjectFilesCollector;
import io.github.rednesto.musicshelf.appSupport.FileAppSupport;

module musicshelf {
    requires kotlin.stdlib;
    requires kotlin.stdlib.jdk7;
    requires kotlin.stdlib.jdk8;

    requires kotlinx.coroutines.core.jvm;
    requires kotlinx.coroutines.javafx;

    requires java.desktop;

    requires javafx.controls;
    requires javafx.fxml;

    requires org.spongepowered.configurate;
    requires org.spongepowered.configurate.hocon;
    requires org.spongepowered.configurate.xml;

    requires geantyref;

    requires org.jetbrains.annotations;

    exports io.github.rednesto.musicshelf;
    exports io.github.rednesto.musicshelf.projectFilesCollectors;
    exports io.github.rednesto.musicshelf.ui;
    exports io.github.rednesto.musicshelf.ui.scenes;

    exports io.github.rednesto.musicshelf.appSupport;
    exports io.github.rednesto.musicshelf.appSupport.builtin;

    uses ProjectFilesCollector;

    provides ProjectFilesCollector with
            io.github.rednesto.musicshelf.projectFilesCollectors.ListedProjectFilesCollector,
            io.github.rednesto.musicshelf.projectFilesCollectors.DirectoryProjectFilesCollector;

    uses FileAppSupport;
}
