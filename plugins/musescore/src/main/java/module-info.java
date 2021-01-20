module musicshelf.musescore {
    requires kotlin.stdlib;

    requires musicshelf;

    provides io.github.rednesto.musicshelf.appSupport.FileAppSupport with
            io.github.rednesto.musicshelf.musescore.Musescore2AppSupport,
            io.github.rednesto.musicshelf.musescore.Musescore3AppSupport;
}
