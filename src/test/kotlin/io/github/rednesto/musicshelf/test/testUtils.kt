package io.github.rednesto.musicshelf.test

import org.junit.jupiter.api.Assertions.*

fun assertEqualsUnordered(expected: Collection<*>, actual: Collection<*>) {
    assertEquals(expected.size, actual.size, "Expected size does not match actual size")
    assertTrue(expected.containsAll(actual))
}
