package com.example.testapp

fun String.levenshteinDistance(other: String) : Int = TextUtil.dist(this, other)

fun String.qwertyMistakes(other: String): Int {
    if (equals(other)) {
        return 0
    }
    if (length != other.length) {
        return Integer.MAX_VALUE
    }

    var mistakes = 0
    for (i in 0 until length) {
        if (get(i) == other[i]) {
            continue
        }
        if (!isAdjacent(get(i), other[i])) {
            return Integer.MAX_VALUE
        }
        mistakes++
    }
    return mistakes
}