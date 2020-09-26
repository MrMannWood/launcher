package com.example.testapp

import java.lang.Exception

val QWERTY = arrayOf(
    arrayOf('1','2','3','4','5','6','7','8','9','0','-','='),
    arrayOf('q','w','e','r','t','y','u','i','o','p','[',']'),
    arrayOf('a','s','d','f','g','h','j','k','l',';','\''),
    arrayOf('z','x','c','v','b','n','m',',','.','/')
)

fun isAdjacent(char: Char, other: Char): Boolean {
    var qwertyIndex = -1
    var arrIndex = -1
    for(charsIndex in QWERTY.indices) {
        val index = QWERTY[charsIndex].indexOf(char)
        if (index >= 0) {
            qwertyIndex = charsIndex
            arrIndex = index
            break
        }
    }

    if (qwertyIndex == -1) {
        throw Exception("Non QWERTY character")
    }

    if (qwertyIndex != 0) {
        if (isAdjacent(QWERTY[qwertyIndex - 1], arrIndex, other)) {
            return true
        }
    }
    if (qwertyIndex < QWERTY.size) {
        if (isAdjacent(QWERTY[qwertyIndex], arrIndex, other)) {
            return true
        }
    }
    if (qwertyIndex < QWERTY.size - 1) {
        if (isAdjacent(QWERTY[qwertyIndex + 1], arrIndex, other)) {
            return true
        }
    }

    return false
}

private fun isAdjacent(chars: Array<Char>, index: Int, char: Char) : Boolean {
    if (index > 0 && char == chars[index - 1]) {
        return true
    }
    if (index < chars.size && char == chars[index]) {
        return true
    }
    if (index < chars.size - 1 && char == chars[index + 1]) {
        return true
    }
    return false
}