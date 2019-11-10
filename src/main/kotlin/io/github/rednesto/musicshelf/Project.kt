package io.github.rednesto.musicshelf

import java.nio.file.Path
import java.util.*

data class Project(val id: UUID, val name: String, val files: Map<String, Path>, val info: Map<String, String>, val groups: Set<String>)
