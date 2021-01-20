module musicshelf.acrobatreader {
    requires kotlin.stdlib;

    requires musicshelf;

    provides io.github.rednesto.musicshelf.appSupport.FileAppSupport with
            io.github.rednesto.musicshelf.acrobatreader.AcrobatReaderAppSupport;
}
