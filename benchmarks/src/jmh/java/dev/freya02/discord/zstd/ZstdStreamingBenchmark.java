package dev.freya02.discord.zstd;

import dev.freya02.discord.zstd.api.DiscordZstdContext;
import dev.freya02.discord.zstd.jni.DiscordZstdJNI;
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
    public static class ShardsState {
        public List<List<TestChunks.Chunk>> shards;

        @Setup
        public void setup() {
            shards = TestChunks.get();
        }
    }

    @State(Scope.Benchmark)
    public static class ZstdDecompressorState {
        @Param({"jni"})
        private String impl;

        public DiscordZstdContext context;

        public byte[] buf;

        @Setup
        public void setup() throws IOException {
            context = switch (impl) {
                case "jni" -> new DiscordZstdJNI().createContext();
                default -> throw new AssertionError("Unknown implementation: " + impl);
            };
            buf = new byte[8192];
        }

        @TearDown
        public void tearDown() {
            context.close();
        }
    }

    @Benchmark
    public void zstd(ZstdDecompressorState decompressorState, ShardsState shardsState, Blackhole blackhole) throws IOException {
        var context = decompressorState.context;
        for (List<TestChunks.Chunk> shard : shardsState.shards) {
            context.reset();
            for (TestChunks.Chunk chunk : shard) {
                try (InputStream inputStream = context.createInputStream(chunk.zstdCompressed())) {
                    blackhole.consume(DataObject.fromJson(inputStream));
                }
            }
        }
    }

    @Benchmark
    public void zstdNoDeser(ZstdDecompressorState decompressorState, ShardsState shardsState, Blackhole blackhole) throws IOException {
        var context = decompressorState.context;
        for (List<TestChunks.Chunk> shard : shardsState.shards) {
            context.reset();
            for (TestChunks.Chunk chunk : shard) {
                try (InputStream inputStream = context.createInputStream(chunk.zstdCompressed())) {
                    while (true) {
                        var read = inputStream.read(decompressorState.buf);
                        blackhole.consume(decompressorState.buf);
                        if (read <= 0) {
                            break;
                        }
                    }
                }
            }
        }
    }



    @State(Scope.Benchmark)
    public static class ZlibDecompressorState {
        public ZlibStreamingDecompressor decompressor;

        public byte[] buf;

        @Setup
        public void setup() {
            decompressor = new ZlibStreamingDecompressor();
            buf = new byte[8192];
        }
    }

    @Benchmark
    public void zlib(ZlibDecompressorState decompressorState, ShardsState shardsState, Blackhole blackhole) throws IOException {
        var decompressor = decompressorState.decompressor;
        for (List<TestChunks.Chunk> shard : shardsState.shards) {
            decompressor.reset();
            for (TestChunks.Chunk chunk : shard) {
                try (InputStream inputStream = decompressor.createInputStream(chunk.zlibCompressed())) {
                    blackhole.consume(DataObject.fromJson(inputStream));
                }
            }
        }
    }

    @Benchmark
    public void zlibNoDeser(ZlibDecompressorState decompressorState, ShardsState shardsState, Blackhole blackhole) throws IOException {
        var decompressor = decompressorState.decompressor;
        var bytes = decompressorState.buf;
        List<List<TestChunks.Chunk>> shards = shardsState.shards;
        for (int shardId = 0; shardId < shards.size(); shardId++) {
            List<TestChunks.Chunk> shard = shards.get(shardId);
            decompressor.reset();

            for (int chunkId = 0; chunkId < shard.size(); chunkId++) {
                TestChunks.Chunk chunk = shard.get(chunkId);

                try {
                    int currentlyDecompressedSize = 0;
                    int expectedDecompressedSize = chunk.decompressed().length;
                    try (InputStream inputStream = decompressor.createInputStream(chunk.zlibCompressed())) {
                        // This is pretty stupid, #available() returns 1 even when there is no output to be read,
                        // we want to avoid handling EOFException as it may be slow and does not represent real world usage,
                        // checking `read < buf.length` is not viable since it can store data internally and returned in the next call.
                        // So, we instead decompress until we have the known decompressed data length.
                        do {
                            var read = inputStream.read(bytes);
                            currentlyDecompressedSize += read;
                            blackhole.consume(bytes);
                        } while (currentlyDecompressedSize < expectedDecompressedSize);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed on chunk %d (total %d) of shard %d".formatted(chunkId, shard.size(), shardId), e);
                }
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
