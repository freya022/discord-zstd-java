package dev.freya02.discord.zstd.generator.random

import net.dv8tion.jda.api.Permission
import kotlin.random.Random

fun Random.nextPermissions() = Permission.entries.takeRandomly(this, 0..Permission.entries.size).let(Permission::getRaw).toString()

inline fun <R> Random.takeRandomly(block: Random.() -> R): R? = if (nextBoolean()) block() else null
