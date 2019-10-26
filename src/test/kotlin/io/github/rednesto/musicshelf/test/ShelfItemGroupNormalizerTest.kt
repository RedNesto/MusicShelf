package io.github.rednesto.musicshelf.test

import io.github.rednesto.musicshelf.utils.normalizeGroup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ShelfItemGroupNormalizerTest {

    @Test
    fun `root group`() {
        doTest("/", "/")
        doTest("/", "/ ")
        doTest("/", " /")
        doTest("/", " / ")
        doTest("/", " //")
        doTest("/", "/ // ")
        doTest("/", "/ / //")
    }

    @Test
    fun `unnecessary characters`() {
        doTest("groupA", "groupA")
        doTest("groupA", "//groupA")
        doTest("groupA", "//groupA//")
        doTest("groupA", "  groupA ")
        doTest("groupA", "/groupA  ")
        doTest("groupA", " groupA/ ")
        doTest("groupA", "groupA/ ")
        doTest("groupA/groupB", "groupA//groupB")
        doTest("groupA/groupB", "groupA /groupB")
        doTest("groupA/groupB", " groupA/ groupB//")
        doTest("groupA/groupB", "groupA / groupB")
        doTest("groupA/groupB", "groupA/ /groupB ")
        doTest("groupA/groupB", "groupA/ / groupB")
        doTest("groupA/groupB", "groupA // groupB//")
        doTest("groupA/groupB", " /groupA/ /groupB")
        doTest("groupA/groupB", "groupA// /groupB")
        doTest("groupA/groupB", "//groupA  //groupB")
    }

    private fun doTest(expected: String, tested: String) {
        assertEquals(expected, normalizeGroup(tested))
    }
}
