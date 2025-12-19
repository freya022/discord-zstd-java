import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.DataInputStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.inputStream
import kotlin.io.path.name
import kotlin.io.path.walk
import kotlin.time.Duration.Companion.nanoseconds

// TODO calculate per-shard with `log_size / entry_size`
private const val dataSize = 90_000_000

private val logger = KotlinLogging.logger { }

private val shardFolderRegex = Regex("""shard-(\d+)-(?:zstd|zlib)""")
private val logFileRegex = Regex("""log-(\d+)\.bin""")

suspend fun main(args: Array<String>) {
    require(args.size == 1) {
        "One argument must be present for the logs input directory (decompression-logs)"
    }

    val dispatcher = Dispatchers.IO.limitedParallelism(12)

    val zstdSmall = DecompressionMetrics(dataSize)
    val zstdBig = DecompressionMetrics(dataSize)
    val zlib = DecompressionMetrics(dataSize)

    coroutineScope {
        val logsDirectory = Path(args[0])
        Files.walk(logsDirectory, 1)
            .filter { it.name.matches(shardFolderRegex) }
            .sorted(Comparator.comparingInt { shardFolder ->
                shardFolderRegex.matchEntire(shardFolder.name)!!.groupValues[1].toInt()
            })
            .forEach { shardFolder ->
                val shardId = shardFolderRegex.matchEntire(shardFolder.name)!!.groupValues[1].toInt()
                val metrics = when (shardId % 3) {
                    0 -> zstdSmall
                    1 -> zstdBig
                    2 -> zlib
                    else -> error("Unhandled shard $shardId")
                }

                launch(dispatcher) {
                    readShard(shardFolder) { entry ->
                        // Take only those with a throughput of 100 bytes per microsecond
                        if (entry.throughput <= 100.0) return@readShard
                        metrics.accept(entry)
                    }
                }
            }
    }

    coroutineScope {
        launch(dispatcher) { zstdSmall.finish() }
        launch(dispatcher) { zstdBig.finish() }
        launch(dispatcher) { zlib.finish() }
    }

    println("zstdSmall = $zstdSmall")
    println("zstdBig = $zstdBig")
    println("zlib = $zlib")

    fun DecompressionEntry.decompressTimeStats(): String = "$timeToDecompress<br>${compressedSize.prettySize()} -> ${decompressedSize.prettySize()}"
    fun DecompressionEntry.compressedStats(): String = "**${compressedSize.prettySize()}** -> ${decompressedSize.prettySize()}<br>$timeToDecompress"
    fun DecompressionEntry.decompressedStats(): String = "${compressedSize.prettySize()} -> **${decompressedSize.prettySize()}**<br>$timeToDecompress"

    println("""
        | Stat | Zlib | Zstd (8K buf) | Zstd (128K buf) |
        |------|------|---------------|-----------------|
        | Entries | ${zlib.addedEntries.pretty()} | ${zstdSmall.addedEntries.pretty()} | ${zstdBig.addedEntries.pretty()} |
        | Total compressed | ${zlib.totalCompressed.prettySize()} | ${zstdSmall.totalCompressed.prettySize()} | ${zstdBig.totalCompressed.prettySize()} |
        | Total decompressed | ${zlib.totalDecompressed.prettySize()} | ${zstdSmall.totalDecompressed.prettySize()} | ${zstdBig.totalDecompressed.prettySize()} |
        | Total time to decompress | ${zlib.totalTimeToDecompress} | ${zstdSmall.totalTimeToDecompress} | ${zstdBig.totalTimeToDecompress} |
        | Min decompress time | ${zlib.minDecompressTime.decompressTimeStats()} | ${zstdSmall.minDecompressTime.decompressTimeStats()} | ${zstdBig.minDecompressTime.decompressTimeStats()} |
        | Average decompress time | ${zlib.averageDecompressTime} | ${zstdSmall.averageDecompressTime} | ${zstdBig.averageDecompressTime} |
        | Median decompress time | ${zlib.medianDecompressTime} | ${zstdSmall.medianDecompressTime} | ${zstdBig.medianDecompressTime} |
        | Max decompress time | ${zlib.maxDecompressTime.decompressTimeStats()} | ${zstdSmall.maxDecompressTime.decompressTimeStats()} | ${zstdBig.maxDecompressTime.decompressTimeStats()} |
        | Min throughput (B/µs) | ${zlib.minThroughput} | ${zstdSmall.minThroughput} | ${zstdBig.minThroughput} |
        | Average throughput (B/µs) | ${zlib.averageThroughput} | ${zstdSmall.averageThroughput} | ${zstdBig.averageThroughput} |
        | Max throughput (B/µs) | ${zlib.maxThroughput} | ${zstdSmall.maxThroughput} | ${zstdBig.maxThroughput} |
        | Min compressed size | ${zlib.minCompressedSize.compressedStats()} | ${zstdSmall.minCompressedSize.compressedStats()} | ${zstdBig.minCompressedSize.compressedStats()} |
        | Average compressed size (B) | ${zlib.averageCompressedSize} | ${zstdSmall.averageCompressedSize} | ${zstdBig.averageCompressedSize} |
        | Median compressed size (B) | ${zlib.medianCompressedSize} | ${zstdSmall.medianCompressedSize} | ${zstdBig.medianCompressedSize} |
        | Max compressed size | ${zlib.maxCompressedSize.compressedStats()} | ${zstdSmall.maxCompressedSize.compressedStats()} | ${zstdBig.maxCompressedSize.compressedStats()} |
        | Min decompressed size | ${zlib.minDecompressedSize.decompressedStats()} | ${zstdSmall.minDecompressedSize.decompressedStats()} | ${zstdBig.minDecompressedSize.decompressedStats()} |
        | Average decompressed size (B) | ${zlib.averageDecompressedSize} | ${zstdSmall.averageDecompressedSize} | ${zstdBig.averageDecompressedSize} |
        | Median decompressed size (B) | ${zlib.medianDecompressedSize} | ${zstdSmall.medianDecompressedSize} | ${zstdBig.medianDecompressedSize} |
        | Max decompressed size | ${zlib.maxDecompressedSize.decompressedStats()} | ${zstdSmall.maxDecompressedSize.decompressedStats()} | ${zstdBig.maxDecompressedSize.decompressedStats()} |
    """.trimIndent())
}

private fun Int.pretty(): String {
    return toLong().pretty()
}

private fun Long.pretty(): String {
    val str = toString()
    if (str.length <= 3) return str

    return buildString {
        str.reversed().forEachIndexed { index, ch ->
            if (index != 0 && index % 3 == 0) {
                append('_')
            }
            append(ch)
        }
    }.reversed()
}

private fun Int.prettySize(): String {
    return toLong().prettySize()
}

private fun Long.prettySize(): String {
    if (this > 10000) {
        var prettySize = this.toDouble() / 1024.0
        var prettyUnit = "KB"
        if (prettySize > 1024.0) {
            prettySize /= 1024.0
            prettyUnit = "MB"
        }
        if (prettySize > 1024.0) {
            prettySize /= 1024.0
            prettyUnit = "GB"
        }
        return "$this B (${"%.1f".format(prettySize)} ${prettyUnit})"
    } else {
        return "$this B"
    }
}

private suspend fun Iterable<Int>.launchEachShard(shardMetricSupplier: (Int) -> Unit) {
    val dispatcher = Dispatchers.IO.limitedParallelism(parallelism = 4, name = "Read shard data")
    coroutineScope {
        this@launchEachShard.forEach { shardNumber ->
            launch(dispatcher) {
                shardMetricSupplier(shardNumber)
            }
        }
    }
}

private fun readShard(shardPath: Path, entryConsumer: (DecompressionEntry) -> Unit) {
    logger.info { "Reading shard ${shardPath.name}" }
    shardPath.walk()
        .filter { it.name.matches(logFileRegex) }
        .sortedBy { logFile ->
            logFileRegex.matchEntire(logFile.name)!!.groupValues[1].toInt()
        }
        .forEachIndexed { logIndex, logFile ->
            val logIndexByte = logIndex.toUByte()
            readLogFile(logFile, logIndexByte, entryConsumer)
        }
}

private fun readLogFile(logFile: Path, logIndex: UByte, entryConsumer: (DecompressionEntry) -> Unit) {
    logFile.inputStream().buffered().let(::DataInputStream).use { input ->
        var entryIndex = 0u
        var available = input.available()
        while (available > 0) {
            val timeToDecompress = input.readLong()
            val compressedSize = input.readInt()
            val decompressedSize = input.readInt()
            input.skipBytes(2) // Separator

            entryConsumer(DecompressionEntry(logIndex, entryIndex++, timeToDecompress.nanoseconds, compressedSize, decompressedSize))

            available -= (8 + 4 + 4 + 2)
            if (available <= 0) {
                available = input.available()
            }
        }
    }
}
