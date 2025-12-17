package dev.freya02.discord.zstd.generator.random

import kotlin.random.Random
import kotlin.random.nextInt

val digitChars = ('0'..'9').toList()
val letterChars = ('a'..'z').toList() + ('A'..'Z').toList()
val alphanumericChars = ('a'..'z').toList() + ('A'..'Z').toList() + "_-".toCharArray().toList()

@JvmName("nextStringFromChars")
fun Random.nextString(chars: List<Char>, range: IntRange) = String(CharArray(nextInt(range)) { chars.random(this) })

@JvmName("nextStringFromWords")
fun Random.nextString(words: List<String>, range: IntRange) = buildString {
    val mutableList = words.toMutableList()
    val n = nextInt(range)
    while (true) {
        val word = mutableList.removeAt(nextInt(mutableList.size))
        if (this.length + word.length + 1 > n || mutableList.isEmpty()) break
        append(word).append(' ')
    }
}.trimEnd()

fun Random.nextUnicodeEmoji(): String = String(nextBytes(nextInt(2, 8) * 2))
