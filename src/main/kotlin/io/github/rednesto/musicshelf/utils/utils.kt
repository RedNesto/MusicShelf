package io.github.rednesto.musicshelf.utils

infix fun Int.within(list: List<*>): Boolean = this >= 0 && this < list.size

fun <E> MutableCollection<E>.without(element: E): MutableList<E> = without(element, mutableListOf())

fun <E, R : MutableCollection<E>> MutableCollection<E>.without(element: E, destination: R): R {
    destination.addAll(this)
    destination.remove(element)
    return destination
}

fun <E> MutableList<E>.addIfAbsent(element: E): Boolean {
    if (!this.contains(element)) {
        return this.add(element)
    }

    return false
}

fun renameToAvoidDuplicates(original: String, existing: Collection<String>): String {
    if (original !in existing) {
        return original
    }

    fun extractDuplicationMarker(key: String): Int {
        val builder = StringBuilder()
        for (i in key.length - 1 downTo 0) {
            val char = key[i]
            if (!char.isDigit()) {
                break
            }

            builder.insert(0, char)
        }

        return builder.toString().toInt()
    }

    val duplicationMarkers = existing
            .filter { it.startsWith(original) && it != original }
            .map { existingKey -> extractDuplicationMarker(existingKey) }
            .sorted()

    val newMarker = (0..duplicationMarkers.size).toList().subtract(duplicationMarkers).first()
    return original + newMarker
}
