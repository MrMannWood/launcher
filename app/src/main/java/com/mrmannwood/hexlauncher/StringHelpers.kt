package com.mrmannwood.hexlauncher

import java.lang.Integer.min

fun String.levenshtein(that: CharSequence): Int {
    val lhsLength = this.length
    val rhsLength = that.length

    var cost = IntArray(lhsLength + 1) { it }
    var newCost = IntArray(lhsLength + 1) { 0 }

    for (i in 1..rhsLength) {
        newCost[0] = i

        for (j in 1..lhsLength) {
            val editCost = if (this[j - 1] == that[i - 1]) 0 else 1

            val costReplace = cost[j - 1] + editCost
            val costInsert = cost[j] + 1
            val costDelete = newCost[j - 1] + 1

            newCost[j] = minOf(costInsert, costDelete, costReplace)
        }

        val swap = cost
        cost = newCost
        newCost = swap
    }

    return cost[lhsLength]
}

fun String.removeChars(chars: CharArray): String {
    return this.toCharArray().filter { !chars.contains(it) }.joinToString("")
}

fun isVersionStringLess(one: String, two: String): Boolean {
    val oneS = one.split(".")
    val twoS = two.split(".")
    for (i in 0 until min(oneS.size, twoS.size)) {
        if (oneS[i] < twoS[i]) {
            return true
        }
    }
    return false
}
