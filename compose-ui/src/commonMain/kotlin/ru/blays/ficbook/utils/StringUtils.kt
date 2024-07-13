package ru.blays.ficbook.utils

fun String.isBlankOrEmpty(): Boolean {
    if(isEmpty()) return true
    return all(Char::isWhitespace)
}

fun String.isNotBlankOrEmpty(): Boolean {
    if(isNotEmpty()) return true
    isNotBlank()
    return all(Char::isWhitespace).not()
}