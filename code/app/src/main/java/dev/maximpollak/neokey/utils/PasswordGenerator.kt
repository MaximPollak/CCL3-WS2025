package dev.maximpollak.neokey.utils

fun generatePassword(
    length: Int,
    upper: Boolean,
    lower: Boolean,
    digits: Boolean,
    symbols: Boolean
): String {
    val upperChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val lowerChars = "abcdefghijklmnopqrstuvwxyz"
    val digitChars = "0123456789"
    val symbolChars = "!@#$%^&*()-_=+[]{}"

    var pool = ""
    if (upper) pool += upperChars
    if (lower) pool += lowerChars
    if (digits) pool += digitChars
    if (symbols) pool += symbolChars

    if (pool.isEmpty()) return ""

    return (1..length)
        .map { pool.random() }
        .joinToString("")
}