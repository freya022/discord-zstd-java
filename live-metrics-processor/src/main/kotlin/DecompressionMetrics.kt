import gnu.trove.list.array.TIntArrayList
import gnu.trove.list.array.TLongArrayList
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds

class DecompressionMetrics(private val entryAmount: Int) {
    var addedEntries = 0

    var totalCompressed: Long = 0
    var totalDecompressed: Long = 0
    var totalTimeToDecompress: Duration = Duration.ZERO

    lateinit var minDecompressTime: DecompressionEntry
    var averageDecompressTime: Duration = Duration.ZERO
    val decompressTimes = TLongArrayList(entryAmount)
    val medianDecompressTime: Duration get() = decompressTimes[decompressTimes.size() / 2].nanoseconds
    lateinit var maxDecompressTime: DecompressionEntry

    // bytes per microsecond
    var minThroughput: Double = 0.0
    var averageThroughput: Double = 0.0
    var maxThroughput: Double = 0.0

    lateinit var minCompressedSize: DecompressionEntry
    var averageCompressedSize: Double = 0.0
    val compressedSizes = TIntArrayList(entryAmount)
    val medianCompressedSize: Int get() = compressedSizes[compressedSizes.size() / 2]
    lateinit var maxCompressedSize: DecompressionEntry

    lateinit var minDecompressedSize: DecompressionEntry
    var averageDecompressedSize: Double = 0.0
    val decompressedSizes = TIntArrayList(entryAmount)
    val medianDecompressedSize: Int get() = decompressedSizes[decompressedSizes.size() / 2]
    lateinit var maxDecompressedSize: DecompressionEntry

    @Synchronized
    fun accept(entry: DecompressionEntry) {
        val entryIndex = ++addedEntries

        totalCompressed += entry.compressedSize
        totalDecompressed += entry.decompressedSize
        totalTimeToDecompress += entry.timeToDecompress

        decompressTimes.add(entry.timeToDecompress.inWholeNanoseconds)
        decompressedSizes.add(entry.decompressedSize)
        compressedSizes.add(entry.compressedSize)

        // https://en.wikipedia.org/wiki/Moving_average#Cumulative_average
        fun newAverage(newEntryValue: Duration, currentAverage: Duration): Duration {
            return currentAverage + ((newEntryValue - currentAverage) / entryIndex)
        }

        fun newAverage(newEntryValue: Double, currentAverage: Double): Double {
            return currentAverage + ((newEntryValue - currentAverage) / entryIndex)
        }

        if (entryIndex == 1) {
            minDecompressTime = entry
            maxDecompressTime = entry
            averageDecompressTime = entry.timeToDecompress

            minThroughput = entry.throughput
            averageThroughput = minThroughput
            maxThroughput = minThroughput

            minCompressedSize = entry
            maxCompressedSize = entry
            averageCompressedSize = entry.compressedSize.toDouble()

            minDecompressedSize = entry
            maxDecompressedSize = entry
            averageDecompressedSize = entry.decompressedSize.toDouble()
        } else {
            minDecompressTime = if (minDecompressTime.timeToDecompress > entry.timeToDecompress) entry else minDecompressTime
            maxDecompressTime = if (maxDecompressTime.timeToDecompress < entry.timeToDecompress) entry else maxDecompressTime
            averageDecompressTime = newAverage(entry.timeToDecompress, averageDecompressTime)

            val newThroughput = entry.throughput
            minThroughput = if (minThroughput > newThroughput) newThroughput else minThroughput
            maxThroughput = if (maxThroughput < newThroughput) newThroughput else maxThroughput
            averageThroughput = newAverage(newThroughput, averageThroughput)

            minCompressedSize = if (minCompressedSize.compressedSize > entry.compressedSize) entry else minCompressedSize
            maxCompressedSize = if (maxCompressedSize.compressedSize < entry.compressedSize) entry else maxCompressedSize
            averageCompressedSize = newAverage(entry.compressedSize.toDouble(), averageCompressedSize)

            minDecompressedSize = if (minDecompressedSize.decompressedSize > entry.decompressedSize) entry else minDecompressedSize
            maxDecompressedSize = if (maxDecompressedSize.decompressedSize < entry.decompressedSize) entry else maxDecompressedSize
            averageDecompressedSize = newAverage(entry.decompressedSize.toDouble(), averageDecompressedSize)
        }
    }

    fun finish() {
        if (decompressTimes.size() != entryAmount) {
            System.err.println("Mismatched entry amount, found ${decompressTimes.size()}, expected $entryAmount")
        }

        decompressTimes.sort()
        decompressedSizes.sort()
        compressedSizes.sort()
    }

    override fun toString(): String {
        return "DecompressionMetrics(\n\taddedEntries=$addedEntries,\n\ttotalCompressed=$totalCompressed,\n\ttotalDecompressed=$totalDecompressed,\n\ttotalTimeToDecompress=$totalTimeToDecompress,\n\n\tminDecompressTime=$minDecompressTime,\n\taverageDecompressTime=$averageDecompressTime,\n\tmedianDecompressTime=$medianDecompressTime,\n\tmaxDecompressTime=$maxDecompressTime,\n\n\tminThroughput=$minThroughput,\n\taverageThroughput=$averageThroughput,\n\tmaxThroughput=$maxThroughput,\n\n\tminCompressedSize=$minCompressedSize,\n\taverageCompressedSize=$averageCompressedSize,\n\tmedianCompressedSize=$medianCompressedSize,\n\tmaxCompressedSize=$maxCompressedSize,\n\n\tminDecompressedSize=$minDecompressedSize,\n\taverageDecompressedSize=$averageDecompressedSize,\n\tmedianDecompressedSize=$medianDecompressedSize,\n\tmaxDecompressedSize=$maxDecompressedSize\n)"
    }
}
