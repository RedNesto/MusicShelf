package io.github.rednesto.musicshelf.test

import io.github.rednesto.musicshelf.ShelfItemFilterData
import io.github.rednesto.musicshelf.ShelfItemFilterDataParser
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ShelfItemFilterDataParserTest {

    @Test
    fun `by keyword`() {
        doTest(expect("name"), "name")
        doTest(expect("a", "name"), "a name")
    }

    @Test
    fun `by quoted keywords`() {
        doTest(expect("quoted:char"), "quoted\":\"char")
        doTest(expect("quoted : char"), "quoted\" : \"char")
        doTest(expect("quoted", ":", "char"), "quoted \":\" char")
        doTest(expect("quoted :", "char"), "quoted\" :\" char")
    }

    @Test
    fun `only info`() {
        doTest(expect("key" to "value"), "key:value")
        doTest(expect("key1" to null), "key1:")
        doTest(expect("key1" to null, "key2" to "value2"), "key1: key2:value2")
        doTest(expect("the key" to "the value"), "\"the key\":\"the value\"")
        doTest(expect("colon: key" to "colon:value"), "\"colon: key\":\"colon:value\"")
    }

    @Test
    fun `mixed input`() {
        doTest(expect(setOf("keyword1", "keyword with spaces"), "key" to "value"), "keyword1 \"keyword with spaces\" key:value")
        doTest(expect(setOf("keyword1", "keyword with spaces"), "key" to "value", "spaced key" to "the_value"),
                """keyword1 "keyword with spaces" key:value "spaced key":the_value""")
        doTest(expect(setOf("keyword1", "colon :keyword", "keyword with spaces"), "key" to "value", "key2" to "colon : value"),
                """keyword1 "keyword with spaces" key:value "colon :keyword" key2:"colon : value"""")
        doTest(expect(setOf("keyword1", "keyword2"), "key1" to null, "key2" to "value2", "key3" to null),
                """key1: keyword1 keyword2 key2:value2 key3:""")
        doTest(expect(setOf("keyword1", "keyword2", "keyword3:"), "key1" to null, "key2" to "value2"),
                """key1: keyword1 key2:value2 keyword2 "keyword3:"""")
    }

    private fun doTest(expected: ShelfItemFilterData, filter: String) {
        val result = ShelfItemFilterDataParser.parseFilter(filter)
        Assertions.assertEquals(expected, result)
    }

    private fun expect(vararg keywords: String) = ShelfItemFilterData(keywords.toSet(), emptyMap())

    private fun expect(vararg infoToSearch: Pair<String, String?>) = ShelfItemFilterData(emptySet(), infoToSearch.toMap())

    private fun expect(keywords: Set<String>, vararg infoToSearch: Pair<String, String?>) = ShelfItemFilterData(keywords, infoToSearch.toMap())
}
