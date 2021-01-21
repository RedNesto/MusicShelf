# MusicShelf ![Automated Tests](https://github.com/RedNesto/MusicShelf/workflows/Automated%20Tests/badge.svg?event=push)

If like me, you have many sheet music spread across storage drives, you might want a way to gather them in one place,
 group them as you please, sort them by attributes (composer, origin, arranger)
 or even link them with their audio/music software project files (and any generated midi/audio file or similar assets).

I made this piece of software for this need, this is based on my needs but I am open to others' suggestions.

**Right now this project is WIP**, expect bugs and non-optimal behaviours (please report them.)

## Building and Running

You will need a JDK 14 distribution installed in order to compile and run the software.

If you are not sure what to use and/or where to get one, I recommend to use [AdoptOpenJDK][AdoptOpenJDK 14].
 Just extract it anywhere you want, but remember where you put it.

You then need to open a command line prompt and:
- set the `JAVA_HOME` environment variable to your JDK 14 installation (its root, not the `bin` directory)
- run `./gradlew run` on any unix-like OS, or `./gradlew.bat run` on Windows. It may take some time to download everything it needs
- it should compile and launch without any issue, you can now do your own things

**Note:** right now there is no way to package a JAR containing everything needed,
 the `run` task is the only way to use it for the moment.

[AdoptOpenJDK 14]: https://adoptopenjdk.net/?variant=openjdk14&jvmVariant=hotspot
