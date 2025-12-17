package dev.freya02.discord.zstd.generator.random

import kotlin.enums.enumEntries
import kotlin.math.min
import kotlin.random.Random
import kotlin.random.nextInt

fun <E, R> Collection<E>.takeRandomly(random: Random, range: IntRange, transform: (E) -> R): Set<R> {
    val n = min(this.size, random.nextInt(range))
    if (n == 0) return emptySet()

    val mutableCollection = this.toMutableList()
    return buildSet(n) {
        repeat(n) {
            val index = random.nextInt(mutableCollection.size)
            val item = mutableCollection.removeAt(index)
            add(transform(item))
        }
    }
}

fun <E> Collection<E>.takeRandomly(random: Random, range: IntRange): Set<E> = takeRandomly(random, range) { it }

inline fun <R> Random.nextList(range: IntRange, block: Int.() -> R) = List(nextInt(range), block)

inline fun <R> Random.nextSet(range: IntRange, block: () -> R): Set<R> {
    val n = nextInt(range)
    return buildSet(n) {
        while (size < n) {
            add(block())
        }
    }
}

inline fun <reified E : Enum<E>> Random.nextEntry() = enumEntries<E>().random(this)
