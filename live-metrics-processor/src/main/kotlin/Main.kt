import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import java.io.DataInputStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.inputStream
import kotlin.io.path.name
import kotlin.io.path.walk
import kotlin.time.Duration.Companion.nanoseconds

private const val entrySize = 8 + 4 + 4 + 2

private val logger = KotlinLogging.logger { }

private val shardFolderRegex = Regex("""shard-(\d+)-(?:zstd|zlib)""")
private val logFileRegex = Regex("""log-(\d+)\.bin""")

suspend fun main(args: Array<String>) {
    require(args.size == 1) {
        "One argument must be present for the logs input directory (decompression-logs)"
    }

    val dispatcher = Dispatchers.IO.limitedParallelism(12)

    val zstdSmallShards = arrayListOf<List<Path>>()
    val zstdBigShards = arrayListOf<List<Path>>()
    val zlibShards = arrayListOf<List<Path>>()

    val logsDirectory = Path(args[0])
    withContext(Dispatchers.IO) {
        Files.walk(logsDirectory, 1)
            .filter { it.name.matches(shardFolderRegex) }
            .sorted(Comparator.comparingInt { shardFolder ->
                shardFolderRegex.matchEntire(shardFolder.name)!!.groupValues[1].toInt()
            })
            .forEach { shardFolder ->
                val shardId = shardFolderRegex.matchEntire(shardFolder.name)!!.groupValues[1].toInt()
                val shards = when (shardId % 3) {
                    0 -> zstdSmallShards
                    1 -> zstdBigShards
                    2 -> zlibShards
                    else -> error("Unhandled shard $shardId")
                }

                shardFolder.walk()
                    .filter { it.name.matches(logFileRegex) }
                    .sortedBy { logFile ->
                        // Uses a timestamp not an index
                        logFileRegex.matchEntire(logFile.name)!!.groupValues[1].toLong()
                    }
                    .toList()
                    .also(shards::add)
            }
    }

    fun List<List<Path>>.computeEntryCount(): Int {
        return sumOf { shard ->
            shard.sumOf { logFile ->
                var retainedEntries = 0
                readLogThroughputs(logFile) { throughput ->
                    if (throughput > 100.0) {
                        retainedEntries++
                    }
                }
                retainedEntries
            }
        }
    }

    val zstdSmallMetrics = DecompressionMetrics(zstdSmallShards.computeEntryCount())
    val zstdBigMetrics = DecompressionMetrics(zstdBigShards.computeEntryCount())
    val zlibMetrics = DecompressionMetrics(zlibShards.computeEntryCount())

    coroutineScope {
        launch(dispatcher) { processShard(zstdSmallShards, zstdSmallMetrics) }
        launch(dispatcher) { processShard(zstdBigShards, zstdBigMetrics) }
        launch(dispatcher) { processShard(zlibShards, zlibMetrics) }
    }

    coroutineScope {
        launch(dispatcher) { zstdSmallMetrics.finish() }
        launch(dispatcher) { zstdBigMetrics.finish() }
        launch(dispatcher) { zlibMetrics.finish() }
    }

    println("zstdSmall = $zstdSmallMetrics")
    println("zstdBig = $zstdBigMetrics")
    println("zlib = $zlibMetrics")

    fun DecompressionEntry.decompressTimeStats(): String = "$timeToDecompress<br>${compressedSize.prettySize()} -> ${decompressedSize.prettySize()}"
    fun DecompressionEntry.compressedStats(): String = "**${compressedSize.prettySize()}** -> ${decompressedSize.prettySize()}<br>$timeToDecompress"
    fun DecompressionEntry.decompressedStats(): String = "${compressedSize.prettySize()} -> **${decompressedSize.prettySize()}**<br>$timeToDecompress"

    println("""
        | Stat | Zlib | Zstd (8K buf) | Zstd (128K buf) |
        |------|------|---------------|-----------------|
        | Entries | ${zlibMetrics.addedEntries.pretty()} | ${zstdSmallMetrics.addedEntries.pretty()} | ${zstdBigMetrics.addedEntries.pretty()} |
        | Total compressed | ${zlibMetrics.totalCompressed.prettySize()} | ${zstdSmallMetrics.totalCompressed.prettySize()} | ${zstdBigMetrics.totalCompressed.prettySize()} |
        | Total decompressed | ${zlibMetrics.totalDecompressed.prettySize()} | ${zstdSmallMetrics.totalDecompressed.prettySize()} | ${zstdBigMetrics.totalDecompressed.prettySize()} |
        | Total time to decompress | ${zlibMetrics.totalTimeToDecompress} | ${zstdSmallMetrics.totalTimeToDecompress} | ${zstdBigMetrics.totalTimeToDecompress} |
        | Min decompress time | ${zlibMetrics.minDecompressTime.decompressTimeStats()} | ${zstdSmallMetrics.minDecompressTime.decompressTimeStats()} | ${zstdBigMetrics.minDecompressTime.decompressTimeStats()} |
        | Average decompress time | ${zlibMetrics.averageDecompressTime} | ${zstdSmallMetrics.averageDecompressTime} | ${zstdBigMetrics.averageDecompressTime} |
        | Median decompress time | ${zlibMetrics.medianDecompressTime} | ${zstdSmallMetrics.medianDecompressTime} | ${zstdBigMetrics.medianDecompressTime} |
        | Max decompress time | ${zlibMetrics.maxDecompressTime.decompressTimeStats()} | ${zstdSmallMetrics.maxDecompressTime.decompressTimeStats()} | ${zstdBigMetrics.maxDecompressTime.decompressTimeStats()} |
        | Min throughput (B/µs) | ${zlibMetrics.minThroughput} | ${zstdSmallMetrics.minThroughput} | ${zstdBigMetrics.minThroughput} |
        | Average throughput (B/µs) | ${zlibMetrics.averageThroughput} | ${zstdSmallMetrics.averageThroughput} | ${zstdBigMetrics.averageThroughput} |
        | Max throughput (B/µs) | ${zlibMetrics.maxThroughput} | ${zstdSmallMetrics.maxThroughput} | ${zstdBigMetrics.maxThroughput} |
        | Min compressed size | ${zlibMetrics.minCompressedSize.compressedStats()} | ${zstdSmallMetrics.minCompressedSize.compressedStats()} | ${zstdBigMetrics.minCompressedSize.compressedStats()} |
        | Average compressed size (B) | ${zlibMetrics.averageCompressedSize} | ${zstdSmallMetrics.averageCompressedSize} | ${zstdBigMetrics.averageCompressedSize} |
        | Median compressed size (B) | ${zlibMetrics.medianCompressedSize} | ${zstdSmallMetrics.medianCompressedSize} | ${zstdBigMetrics.medianCompressedSize} |
        | Max compressed size | ${zlibMetrics.maxCompressedSize.compressedStats()} | ${zstdSmallMetrics.maxCompressedSize.compressedStats()} | ${zstdBigMetrics.maxCompressedSize.compressedStats()} |
        | Min decompressed size | ${zlibMetrics.minDecompressedSize.decompressedStats()} | ${zstdSmallMetrics.minDecompressedSize.decompressedStats()} | ${zstdBigMetrics.minDecompressedSize.decompressedStats()} |
        | Average decompressed size (B) | ${zlibMetrics.averageDecompressedSize} | ${zstdSmallMetrics.averageDecompressedSize} | ${zstdBigMetrics.averageDecompressedSize} |
        | Median decompressed size (B) | ${zlibMetrics.medianDecompressedSize} | ${zstdSmallMetrics.medianDecompressedSize} | ${zstdBigMetrics.medianDecompressedSize} |
        | Max decompressed size | ${zlibMetrics.maxDecompressedSize.decompressedStats()} | ${zstdSmallMetrics.maxDecompressedSize.decompressedStats()} | ${zstdBigMetrics.maxDecompressedSize.decompressedStats()} |
    """.trimIndent())
}

private fun CoroutineScope.processShard(shards: List<List<Path>>, metrics: DecompressionMetrics) {
    var i = 0
    for (shard in shards) {
        val shardId = i++

        shard.forEachIndexed { logIndex, logFile ->
            launch {
                logger.info { "Reading shard $shardId file $logIndex" }

                val logIndexByte = logIndex.toUByte()
                readLogFile(logFile, logIndexByte) { entry ->
                    // Take only those with a throughput of 100 bytes per microsecond
                    if (entry.throughput <= 100.0) return@readLogFile
                    metrics.accept(entry)
                }
            }
        }
    }
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

            available -= entrySize
            if (available <= 0) {
                available = input.available()
            }
        }
    }
}

private fun readLogThroughputs(logFile: Path, consumer: (throughput: Double) -> Unit) {
    logFile.inputStream().buffered().let(::DataInputStream).use { input ->
        var available = input.available()
        while (available > 0) {
            val timeToDecompress = input.readLong()
            input.skipBytes(4) // Compressed size
            val decompressedSize = input.readInt()
            input.skipBytes(2) // Separator

            consumer(decompressedSize / timeToDecompress.toDouble() * 1000.0)

            available -= entrySize
            if (available <= 0) {
                available = input.available()
            }
        }
    }
}
