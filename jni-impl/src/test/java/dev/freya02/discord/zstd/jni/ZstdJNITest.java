package dev.freya02.discord.zstd.jni;

import dev.freya02.discord.zstd.TestChunks;
import dev.freya02.discord.zstd.api.DiscordZstdProvider;
import dev.freya02.discord.zstd.api.ZstdContext;
import dev.freya02.discord.zstd.api.ZstdDecompressor;
import dev.freya02.discord.zstd.api.ZstdDecompressorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class ZstdJNITest {

    private static List<TestChunks.Chunk> chunks;

    @BeforeAll
    public static void setup() {
        chunks = TestChunks.get(TestChunks.Compression.ZSTD);
    }

    @Test
    public void test_decompression() {
        ZstdDecompressorFactory factory = DiscordZstdProvider.get().createDecompressorFactory(ZstdDecompressor.DEFAULT_BUFFER_SIZE);
        ZstdDecompressor decompressor = factory.create();
        for (TestChunks.Chunk chunk : chunks) {
            final byte[] actual = decompressor.decompress(chunk.getCompressed());
            final byte[] expected = chunk.getDecompressed();
            assertArrayEquals(expected, actual);
        }
    }

    @Test
    public void test_input_stream() throws IOException {
        ZstdContext context = DiscordZstdProvider.get().createContext();
        for (TestChunks.Chunk chunk : chunks) {
            try (InputStream stream = context.createInputStream(chunk.getCompressed())) {
                final byte[] actual = stream.readAllBytes();
                final byte[] expected = chunk.getDecompressed();
                assertArrayEquals(expected, actual);
            }
        }

        context.close();
    }
}
