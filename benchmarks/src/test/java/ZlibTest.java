import dev.freya02.discord.zstd.TestChunks;
import net.dv8tion.jda.internal.utils.IOUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

public class ZlibTest {
    @Test
    public void test_input_stream() {
        var bytes = new byte[8192];
        var decompressor = new ZlibStreamingDecompressor();
        List<List<TestChunks.Chunk>> shards = TestChunks.get();

        for (int shardId = 0; shardId < shards.size(); shardId++) {
            List<TestChunks.Chunk> chunks = shards.get(shardId);

            decompressor.reset();
            for (int chunkId = 0; chunkId < chunks.size(); chunkId++) {
                TestChunks.Chunk chunk = chunks.get(chunkId);

                int currentlyDecompressedSize = 0;
                int expectedDecompressedSize = chunk.decompressed().length;
                try (InputStream inputStream = decompressor.createInputStream(chunk.zlibCompressed())) {
                    do {
                        var read = inputStream.read(bytes);
                        if (read == -1) {
                            break;
                        }
                    } while (currentlyDecompressedSize < expectedDecompressedSize);
                } catch (Exception e) {
                    throw new RuntimeException("Failed on chunk %d (total %d) of shard %d (total %d)".formatted(chunkId, chunks.size(), shardId, shards.size()), e);
                }
            }
        }
    }

    @Test
    public void test_output_stream() {
        var inflater = new Inflater();
        List<List<TestChunks.Chunk>> shards = TestChunks.get();

        for (int shardId = 0; shardId < shards.size(); shardId++) {
            List<TestChunks.Chunk> chunks = shards.get(shardId);

            inflater.reset();
            for (int chunkId = 0; chunkId < chunks.size(); chunkId++) {
                TestChunks.Chunk chunk = chunks.get(chunkId);

                var baos = new ByteArrayOutputStream();
                try (var ios = new InflaterOutputStream(baos, inflater)) {
                    ios.write(chunk.zlibCompressed());
                    Assertions.assertArrayEquals(baos.toByteArray(), chunk.decompressed());
                } catch (Exception e) {
                    throw new RuntimeException("Failed on chunk %d (total %d) of shard %d (total %d)".formatted(chunkId, chunks.size(), shardId, shards.size()), e);
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
            byte[] finalData = data;
            return new InputStream() {
                {
                    inflater.setInput(finalData);
                }

                @Override
                public int read() throws IOException {
                    byte[] buf = new byte[1];
                    return read(buf, 0, 1);
                }

                @Override
                public int read(@NotNull byte[] b, int off, int len) throws IOException {
                    try {
                        if (off > 0) throw new RuntimeException("off > 0");
                        if (inflater.finished()) throw new RuntimeException("Inflater has finished somehow");
                        if (inflater.needsInput()) return -1;
                        if (inflater.needsDictionary()) throw new RuntimeException("Inflater needs dict???");

                        return inflater.inflate(b, off, len);
                    } catch (Throwable e) {
                        throw new IOException("Unable to decompress, %d read, %d written %d remaining".formatted(inflater.getBytesRead(), inflater.getBytesWritten(), inflater.getRemaining()), e);
                    }
                }
            };
        }
    }
}
