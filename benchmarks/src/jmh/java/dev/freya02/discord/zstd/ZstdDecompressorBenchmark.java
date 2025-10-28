package dev.freya02.discord.zstd;

import dev.freya02.discord.zstd.api.ZstdDecompressor;
import dev.freya02.discord.zstd.api.ZstdDecompressorFactory;
import dev.freya02.discord.zstd.api.ZstdNativesLoader;
import dev.freya02.discord.zstd.ffm.ZstdFFMDecompressorFactory;
import dev.freya02.discord.zstd.jna.ZstdJNADecompressorFactory;
import net.dv8tion.jda.internal.utils.compress.ZlibDecompressor;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.DataFormatException;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class ZstdDecompressorBenchmark {

    private static final int MAX_BUFFER_SIZE = 2048;

    @State(Scope.Benchmark)
    public static class ZstdDecompressorState {
        @Param({"ffm", "jna"})
        private String impl;

        public ZstdDecompressor decompressor;

        @Setup
        public void setup() throws IOException {
            ZstdNativesLoader.loadFromJar();
            ZstdDecompressorFactory factory = switch (impl) {
                case "ffm" -> new ZstdFFMDecompressorFactory();
                case "jna" -> new ZstdJNADecompressorFactory();
                default -> throw new AssertionError("Unknown implementation: " + impl);
            };
            decompressor = factory.get(MAX_BUFFER_SIZE);
        }
    }

    @State(Scope.Benchmark)
    public static class ZstdChunksState {
        public List<TestChunks.Chunk> chunks;

        @Setup
        public void setup() {
            chunks = TestChunks.get(TestChunks.Compression.ZSTD);
        }
    }

    @Benchmark
    public void zstd(ZstdDecompressorState decompressorState, ZstdChunksState chunksState, Blackhole blackhole) {
        var decompressor = decompressorState.decompressor;
        decompressor.reset();
        // Can't make a benchmark per-message (so we can see scaling based on message sizes
        //  as this uses a streaming decompressor, meaning this requires previous inputs
        for (TestChunks.Chunk chunk : chunksState.chunks)
            blackhole.consume(decompressor.decompress(chunk.getCompressed()));
    }



    @State(Scope.Benchmark)
    public static class ZlibDecompressorState {
        public ZlibDecompressor decompressor;

        @Setup
        public void setup() {
            decompressor = new ZlibDecompressor(MAX_BUFFER_SIZE);
        }
    }

    @State(Scope.Benchmark)
    public static class ZlibChunksState {
        public List<TestChunks.Chunk> chunks;

        @Setup
        public void setup() {
            chunks = TestChunks.get(TestChunks.Compression.ZLIB);
        }
    }

    @Benchmark
    public void zlib(ZlibDecompressorState decompressorState, ZlibChunksState chunksState, Blackhole blackhole) throws DataFormatException {
        var decompressor = decompressorState.decompressor;
        decompressor.reset();
        // Can't make a benchmark per-message (so we can see scaling based on message sizes
        //  as this uses a streaming decompressor, meaning this requires previous inputs
        for (TestChunks.Chunk chunk : chunksState.chunks)
            blackhole.consume(decompressor.decompress(chunk.getCompressed()));
    }
}
