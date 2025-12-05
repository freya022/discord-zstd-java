package dev.freya02.discord.zstd;

import dev.freya02.discord.zstd.api.DiscordZstdDecompressor;
import dev.freya02.discord.zstd.api.DiscordZstdDecompressorFactory;
import dev.freya02.discord.zstd.api.DiscordZstdNativesLoader;
import dev.freya02.discord.zstd.jni.DiscordZstdJNI;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.IOUtil;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class ZstdDecompressorBenchmark {

    private static final int ZSTD_BUFFER_SIZE = DiscordZstdDecompressor.DEFAULT_BUFFER_SIZE;
    private static final int ZLIB_BUFFER_SIZE = 2048; // JDA default

    @State(Scope.Benchmark)
    public static class ZstdDecompressorState {
        @Param({"jni"})
        private String impl;

        public DiscordZstdDecompressor decompressor;

        @Setup
        public void setup() throws IOException {
            DiscordZstdNativesLoader.loadFromJar();
            DiscordZstdDecompressorFactory factory = switch (impl) {
                case "jni" -> new DiscordZstdJNI().createDecompressorFactory(ZSTD_BUFFER_SIZE);
                default -> throw new AssertionError("Unknown implementation: " + impl);
            };
            decompressor = factory.create();
        }

        @TearDown
        public void tearDown() {
            decompressor.close();
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
            blackhole.consume(DataObject.fromJson(decompressor.decompress(chunk.getCompressed())));
    }



    @State(Scope.Benchmark)
    public static class ZlibDecompressorState {
        public ZlibDecompressor decompressor;

        @Setup
        public void setup() {
            decompressor = new ZlibDecompressor(ZLIB_BUFFER_SIZE);
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
        for (TestChunks.Chunk chunk : chunksState.chunks) {
            blackhole.consume(DataObject.fromJson(decompressor.decompress(chunk.getCompressed())));
        }
    }

    public static class ZlibDecompressor
    {
        private static final int Z_SYNC_FLUSH = 0x0000FFFF;

        private final int maxBufferSize;
        private final Inflater inflater = new Inflater();
        private ByteBuffer flushBuffer = null;
        private SoftReference<ByteArrayOutputStream> decompressBuffer = null;

        public ZlibDecompressor(int maxBufferSize)
        {
            this.maxBufferSize = maxBufferSize;
        }

        private SoftReference<ByteArrayOutputStream> newDecompressBuffer()
        {
            return new SoftReference<>(new ByteArrayOutputStream(Math.min(1024, maxBufferSize)));
        }

        private ByteArrayOutputStream getDecompressBuffer()
        {
            // If no buffer has been allocated yet we do that here (lazy init)
            if (decompressBuffer == null)
                decompressBuffer = newDecompressBuffer();
            // Check if the buffer has been collected by the GC or not
            ByteArrayOutputStream buffer = decompressBuffer.get();
            if (buffer == null) // create a ne buffer because the GC got it
                decompressBuffer = new SoftReference<>(buffer = new ByteArrayOutputStream(Math.min(1024, maxBufferSize)));
            return buffer;
        }

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

        public byte[] decompress(byte[] data) throws DataFormatException
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
            //Get the compressed message and inflate it
            //We use the same buffer here to optimize gc use
            ByteArrayOutputStream buffer = getDecompressBuffer();
            try (InflaterOutputStream decompressor = new InflaterOutputStream(buffer, inflater))
            {
                // This decompressor writes the received data and inflates it
                decompressor.write(data);
                // Once decompressed we re-interpret the data as a String which can be used for JSON parsing
                return buffer.toByteArray();
            }
            catch (IOException e)
            {
                // Some issue appeared during decompression that caused a failure
                throw (DataFormatException) new DataFormatException("Malformed").initCause(e);
            }
            finally
            {
                // When done with decompression we want to reset the buffer so it can be used again later
                if (buffer.size() > maxBufferSize)
                    decompressBuffer = newDecompressBuffer();
                else
                    buffer.reset();
            }
        }
    }
}
