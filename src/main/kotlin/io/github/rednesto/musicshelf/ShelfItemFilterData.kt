package io.github.rednesto.musicshelf

import io.github.rednesto.musicshelf.utils.isRootGroup
import io.github.rednesto.musicshelf.utils.normalizeGroup

data class ShelfItemFilterData(val keywords: Set<String>, val groups: Set<String>, val info: Map<String, String?>)

object ShelfItemFilterDataParser {
    /**
     * See ShelfItemFilterParserTest to get an idea of what is expected of this parser
     */
    fun parseFilter(filter: String): ShelfItemFilterData {
        val trimmedFilter = filter.trim()
        val keywords = mutableSetOf<String>()
        val groups = mutableSetOf<String>()
        val info: MutableMap<String, String?> = mutableMapOf()

        var isInQuote = false
        val currentWord = StringBuilder()
        var pendingInfoKey: String? = null
        val group = StringBuilder()

        fun addGroup(rawGroup: String) {
            var groupToAdd = normalizeGroup(rawGroup)
            if (isRootGroup(groupToAdd)) {
                groups.add("/")
                return
            }

            if (rawGroup.endsWith('/')) {
                groupToAdd += '/'
            }
            groups.add(groupToAdd)
        }

        for (c in trimmedFilter) {
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

            if (c == '/' && pendingInfoKey == null) {
                if (currentWord.isNotEmpty()) {
                    group.append(currentWord)
                    currentWord.clear()
                }
                group.append('/')
                continue
            }

            if (c.isWhitespace()) {
                if (pendingInfoKey != null) {
                    if (currentWord.isEmpty()) {
                        info[pendingInfoKey] = null
                    } else {
                        info[pendingInfoKey] = currentWord.toString()
                        currentWord.clear()
                    }

                    pendingInfoKey = null
                }

                if (group.isNotEmpty()) {
                    if (!group.startsWith('/')) {
                        group.append('/')
                    }
                    addGroup(group.append(currentWord).toString())
                    currentWord.clear()
                    group.clear()
                }

                if (currentWord.isNotEmpty()) {
                    keywords.add(currentWord.toString())
                    currentWord.clear()
                }
                continue
            }

            currentWord.append(c)
        }

        if (currentWord.isNotEmpty()) {
            if (pendingInfoKey != null) {
                info[pendingInfoKey] = currentWord.toString()
                pendingInfoKey = null
                currentWord.clear()
            }

            if (currentWord.isNotEmpty()) {
                val currentWordString = currentWord.toString()
                if (group.isNotEmpty()) {
                    if (!group.startsWith('/')) {
                        group.append('/')
                    }
                    group.append(currentWordString)
                    currentWord.clear()
                } else if (currentWordString.contains('/')) {
                    addGroup(currentWordString)
                    currentWord.clear()
                }
            }

            if (currentWord.isNotEmpty()) {
                keywords.add(currentWord.toString())
            }
        }

        if (group.isNotEmpty()) {
            addGroup(group.toString())
        }

        if (pendingInfoKey != null) {
            info[pendingInfoKey] = null
        }

        return ShelfItemFilterData(keywords, groups, info)
    }
}
