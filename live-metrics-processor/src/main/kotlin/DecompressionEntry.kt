import kotlin.time.Duration

data class DecompressionEntry(val logIndex: UByte, val entryIndex: UInt, val timeToDecompress: Duration, val compressedSize: Int, val decompressedSize: Int)

/** B/µs */
val DecompressionEntry.throughput: Double
    get() = decompressedSize / timeToDecompress.inWholeNanoseconds.toDouble() * 1000.0
