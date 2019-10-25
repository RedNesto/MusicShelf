package io.github.rednesto.musicshelf.utils

infix fun Int.within(list: List<*>): Boolean = this >= 0 && this < list.size

fun <E> MutableCollection<E>.without(element: E): MutableList<E> = without(element, mutableListOf())

fun <E, R : MutableCollection<E>> MutableCollection<E>.without(element: E, destination: R): R {
    destination.addAll(this)
    destination.remove(element)
    return destination
}

fun <E> MutableList<E>.addIfAbsent(element: E) {
    if (!this.contains(element)) {
        this.add(element)
    }
}
