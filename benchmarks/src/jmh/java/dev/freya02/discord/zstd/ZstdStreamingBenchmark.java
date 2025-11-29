package dev.freya02.discord.zstd;

import dev.freya02.discord.zstd.api.ZstdContext;
import dev.freya02.discord.zstd.api.ZstdNativesLoader;
import dev.freya02.discord.zstd.jni.ZstdJNIContextFactoryProvider;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.IOUtil;
import org.jetbrains.annotations.Nullable;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class ZstdStreamingBenchmark {

    @State(Scope.Benchmark)
    public static class ZstdDecompressorState {
        @Param({"jni"})
        private String impl;

        public ZstdContext context;

        @Setup
        public void setup() throws IOException {
            ZstdNativesLoader.loadFromJar();
            var factory = switch (impl) {
                case "jni" -> new ZstdJNIContextFactoryProvider().get();
                default -> throw new AssertionError("Unknown implementation: " + impl);
            };
            context = factory.create();
        }

        @TearDown
        public void tearDown() {
            context.close();
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
    public void zstd(ZstdDecompressorState decompressorState, ZstdChunksState chunksState, Blackhole blackhole) throws IOException {
        var context = decompressorState.context;
        context.reset();
        for (TestChunks.Chunk chunk : chunksState.chunks) {
            try (InputStream inputStream = context.createInputStream(chunk.getCompressed())) {
                blackhole.consume(DataObject.fromJson(inputStream));
            }
        }
    }



    @State(Scope.Benchmark)
    public static class ZlibDecompressorState {
        public ZlibStreamingDecompressor decompressor;

        @Setup
        public void setup() {
            decompressor = new ZlibStreamingDecompressor();
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
    public void zlib(ZlibDecompressorState decompressorState, ZlibChunksState chunksState, Blackhole blackhole) throws IOException {
        var decompressor = decompressorState.decompressor;
        decompressor.reset();
        // Can't make a benchmark per-message (so we can see scaling based on message sizes
        //  as this uses a streaming decompressor, meaning this requires previous inputs
        for (TestChunks.Chunk chunk : chunksState.chunks) {
            try (InputStream inputStream = decompressor.createInputStream(chunk.getCompressed())) {
                blackhole.consume(DataObject.fromJson(inputStream));
            }
        }
    }

    public static class ZlibStreamingDecompressor
    {
        private static final int Z_SYNC_FLUSH = 0x0000FFFF;

        private final Inflater inflater = new Inflater();
        private ByteBuffer flushBuffer = null;

        private boolean isFlush(byte[] data)
        {
            if (data.length < 4)
                return false;
            int suffix = IOUtil.getIntBigEndian(data, data.length - 4);
            return suffix == Z_SYNC_FLUSH;
        }

        private void buffer(byte[] data)
        {
            if (flushBuffer == null)
                flushBuffer = ByteBuffer.allocate(data.length * 2);

            //Ensure the capacity can hold the new data, ByteBuffer doesn't grow automatically
            if (flushBuffer.capacity() < data.length + flushBuffer.position())
            {
                //Flip to make it a read buffer
                flushBuffer.flip();
                //Reallocate for the new capacity
                flushBuffer = IOUtil.reallocate(flushBuffer, (flushBuffer.capacity() + data.length) * 2);
            }

            flushBuffer.put(data);
        }

        public void reset()
        {
            inflater.reset();
        }

        @Nullable
        public InputStream createInputStream(byte[] data)
        {
            //Handle split messages
            if (!isFlush(data))
            {
                //There is no flush suffix so this is not the end of the message
                buffer(data);
                return null; // signal failure to decompress
            }
            else if (flushBuffer != null)
            {
                //This has a flush suffix and we have an incomplete package buffered
                //concatenate the package with the new data and decompress it below
                buffer(data);
                byte[] arr = flushBuffer.array();
                data = new byte[flushBuffer.position()];
                System.arraycopy(arr, 0, data, 0, data.length);
                flushBuffer = null;
            }
            return new InflaterInputStream(new ByteArrayInputStream(data), inflater);
        }
    }
}
