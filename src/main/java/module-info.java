module musicshelf {
    requires kotlin.stdlib;

    requires javafx.controls;
    requires javafx.fxml;
    requires org.jetbrains.annotations;

    exports io.github.rednesto.musicshelf;
    exports io.github.rednesto.musicshelf.ui;
    exports io.github.rednesto.musicshelf.ui.scenes;
}
