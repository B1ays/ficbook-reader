package ru.blays.ficbook.reader.shared.utils

internal inline fun listOfInts(count: Int): List<Int> = List(count) { it }
internal inline fun listOfLongs(count: Int): List<Long> = List(count, Int::toLong)