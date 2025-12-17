package dev.freya02.discord.zstd.generator.random

import kotlin.random.Random
import kotlin.random.nextInt

typealias Chances = Int

fun <R> Random.fromDistribution(vararg distributions: Pair<Chances, R>): R {
    val count = distributions.sumOf { (chances, _) -> chances }
    val index = nextInt(0..<count)
    var offset = 0
    for ((chances, range) in distributions) {
        if (index <= offset + chances) {
            return range
        }
        offset += chances
    }

    throw AssertionError("Could not find index $index in distributions: ${distributions.contentToString()}")
}
