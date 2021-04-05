package com.mrmannwood.hexlauncher

val QWERTY = mapOf(
    'q' to listOf('w', 'a'),
    'w' to listOf('q', 'e', 'a', 's'),
    'e' to listOf('w', 'r', 's', 'd'),
    'r' to listOf('e', 't', 'd', 'f'),
    't' to listOf('r', 'y', 'f', 'g'),
    'y' to listOf('t', 'u', 'g', 'h'),
    'u' to listOf('y', 'i', 'h', 'j'),
    'i' to listOf('u', 'o', 'j', 'k'),
    'o' to listOf('i', 'p', 'k', 'l'),
    'p' to listOf('o', 'l'),
    'a' to listOf('q', 'w', 's', 'z'),
    's' to listOf('w', 'e', 'a', 'd', 'z', 'x'),
    'd' to listOf('e', 'r', 's', 'f', 'x', 'c', 'z'),
    'f' to listOf('r', 't', 'd', 'g', 'c', 'v', 'x'),
    'g' to listOf('t', 'y', 'f', 'h', 'c', 'v', 'b'),
    'h' to listOf('y', 'u', 'g', 'j', 'v', 'b', 'n'),
    'j' to listOf('u', 'i', 'h', 'k', 'b', 'n', 'm'),
    'k' to listOf('i', 'o', 'j', 'l', 'n', 'm'),
    'l' to listOf('o', 'p', 'k', 'm'),
    'z' to listOf('a', 's', 'd', 'x'),
    'x' to listOf('s', 'd', 'f', 'z', 'c'),
    'c' to listOf('d', 'f', 'g', 'x', 'v'),
    'v' to listOf('c', 'f', 'g', 'h', 'b'),
    'b' to listOf('g', 'h', 'j', 'v', 'n'),
    'n' to listOf('h', 'j', 'k', 'b', 'm'),
    'm' to listOf('j', 'k', 'l', 'n')
)

fun isAdjacent(char: Char, other: Char): Boolean = QWERTY[char]?.contains(other) ?: false