package io.github.rednesto.musicshelf.test

import io.github.rednesto.musicshelf.ShelfItemFilterData
import io.github.rednesto.musicshelf.ShelfItemFilterDataParser
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ShelfItemFilterDataParserTest {

    @Test
    fun `by keyword`() {
        doTest(expectKeywords("name"), "name")
        doTest(expectKeywords("a", "name"), "a name")
    }

    @Test
    fun `by quoted keywords`() {
        doTest(expectKeywords("quoted:char"), "quoted\":\"char")
        doTest(expectKeywords("quoted : char"), "quoted\" : \"char")
        doTest(expectKeywords("quoted", ":", "char"), "quoted \":\" char")
        doTest(expectKeywords("quoted :", "char"), "quoted\" :\" char")
    }

    @Test
    fun `only groups`() {
        doTest(expectGroup("/"), "/")
        doTest(expectGroup("groupA"), "/groupA")
        doTest(expectGroup("groupA/"), "groupA/")
        doTest(expectGroup("groupA/"), "/groupA/")
        doTest(expectGroup("groupA/groupB"), "/groupA/groupB")
        doTest(expectGroup("groupA/groupB/"), "groupA/groupB/")

        doTest(expectGroup("groupA", "groupB"), "/groupA /groupB")
        doTest(expectGroup("groupA/", "groupB"), "/groupA/ /groupB")
        doTest(expectGroup("groupA", "groupB/groupC"), "/groupA groupB/groupC")
        doTest(expectGroup("/", "groupA", "groupB/groupC"), "/ /groupA groupB/groupC")

        doTest(expectGroup("/"), "\"/\"")
        doTest(expectGroup("segment 1/segment 2"), """/"segment 1"/"segment 2"""")
        doTest(expectGroup("segment 1/segment 2/"), """"segment 1/segment 2/"""")
        doTest(expectGroup("space group", "another group"), """/"space group" "/another group"""")
    }

    @Test
    fun `only info`() {
        doTest(expectInfo("key" to "value"), "key:value")
        doTest(expectInfo("key1" to null), "key1:")
        doTest(expectInfo("key1" to null, "key2" to "value2"), "key1: key2:value2")
        doTest(expectInfo("the key" to "the value"), "\"the key\":\"the value\"")
        doTest(expectInfo("colon: key" to "colon:value"), "\"colon: key\":\"colon:value\"")

        doTest(expectInfo("key" to "slash/value"), "key:\"slash/value\"")
    }

    @Test
    fun `mixed input`() {
        doTest(ShelfItemFilterData(setOf("keyword1", "keyword with spaces"), emptySet(), mapOf("key" to "value")), "keyword1 \"keyword with spaces\" key:value")
        doTest(ShelfItemFilterData(setOf("keyword1", "keyword with spaces"), emptySet(), mapOf("key" to "value", "spaced key" to "the_value")),
                """keyword1 "keyword with spaces" key:value "spaced key":the_value""")
        doTest(ShelfItemFilterData(setOf("keyword1", "colon :keyword", "keyword with spaces"), emptySet(), mapOf("key" to "value", "key2" to "colon : value")),
                """keyword1 "keyword with spaces" key:value "colon :keyword" key2:"colon : value"""")
        doTest(ShelfItemFilterData(setOf("keyword1", "keyword2"), emptySet(), mapOf("key1" to null, "key2" to "value2", "key3" to null)),
                """key1: keyword1 keyword2 key2:value2 key3:""")
        doTest(ShelfItemFilterData(setOf("keyword1", "keyword2", "keyword3:"), emptySet(), mapOf("key1" to null, "key2" to "value2")),
                """key1: keyword1 key2:value2 keyword2 "keyword3:"""")
        doTest(ShelfItemFilterData(setOf("keyword1"), setOf("/", "groupA"), mapOf("key1" to null, "key2" to "value2")),
                """key1: keyword1 key2:value2 / /groupA""")
        doTest(ShelfItemFilterData(emptySet(), setOf("groupA", "/"), mapOf("key1" to "/slashVal")),
                """key1:/slashVal /groupA /""")
    }

    private fun doTest(expected: ShelfItemFilterData, filter: String) {
        val result = ShelfItemFilterDataParser.parseFilter(filter)
        Assertions.assertEquals(expected, result)
    }

    private fun expectKeywords(vararg keywords: String) = ShelfItemFilterData(keywords.toSet(), emptySet(), emptyMap())

    private fun expectGroup(vararg groups: String) = ShelfItemFilterData(emptySet(), groups.toSet(), emptyMap())

    private fun expectInfo(vararg info: Pair<String, String?>) = ShelfItemFilterData(emptySet(), emptySet(), info.toMap())
}
