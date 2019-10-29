package io.github.rednesto.musicshelf

data class ShelfItemFilterData(val keywords: Set<String>, val infoToSearch: Map<String, String?>)

object ShelfItemFilterDataParser {
    /**
     * See ShelfItemFilterParserTest to get an idea of what is expected of this parser
     */
    fun parseFilter(filter: String): ShelfItemFilterData {
        val keywords = mutableSetOf<String>()
        val infoToSearch: MutableMap<String, String?> = mutableMapOf()

        var isInQuote = false
        val currentWord = StringBuilder()
        var pendingInfoKey: String? = null
        for (c in filter) {
            if (c == '"') {
                isInQuote = !isInQuote
                continue
            }

            if (isInQuote) {
                currentWord.append(c)
                continue
            }

            if (c == ':') {
                if (currentWord.isNotEmpty()) {
                    pendingInfoKey = currentWord.toString()
                    currentWord.clear()
                }
                continue
            }

            if (c.isWhitespace()) {
                if (pendingInfoKey != null) {
                    if (currentWord.isEmpty()) {
                        infoToSearch[pendingInfoKey] = null
                    } else {
                        infoToSearch[pendingInfoKey] = currentWord.toString()
                    }

                    pendingInfoKey = null
                } else {
                    keywords.add(currentWord.toString())
                }
                currentWord.clear()
                continue
            }

            currentWord.append(c)
        }

        if (currentWord.isNotEmpty()) {
            if (pendingInfoKey != null) {
                infoToSearch[pendingInfoKey] = currentWord.toString()
                pendingInfoKey = null
            } else {
                keywords.add(currentWord.toString())
            }
        }

        if (pendingInfoKey != null) {
            infoToSearch[pendingInfoKey] = null
        }

        return ShelfItemFilterData(keywords, infoToSearch)
    }
}
