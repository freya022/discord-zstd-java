package dev.freya02.discord.zstd.generator.random

import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.random.Random

private val hexChars = "01234567890abcdef".toCharArray()

fun Random.nextSnowflake() = nextLong(100000000000000, Long.MAX_VALUE).toString()
fun Random.nextInstant(): Instant = Instant.now().plus(nextLong(0, 3600), ChronoUnit.SECONDS)
fun Random.nextVersion() = nextLong(0, 1000000)
fun Random.nextHash() = String(CharArray(32) { hexChars.random(this) })
