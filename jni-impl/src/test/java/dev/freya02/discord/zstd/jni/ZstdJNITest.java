package dev.freya02.discord.zstd.jni;

import dev.freya02.discord.zstd.TestChunks;
import dev.freya02.discord.zstd.api.ZstdDecompressor;
import dev.freya02.discord.zstd.api.ZstdDecompressorFactory;
import dev.freya02.discord.zstd.api.ZstdNativesLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ZstdJNITest {

    private static List<TestChunks.Chunk> chunks;

    @BeforeAll
    public static void setup() {
        chunks = TestChunks.get(TestChunks.Compression.ZSTD);
    }

    @Test
    public void test_decompression() throws IOException {
        assertTrue(ZstdNativesLoader.loadFromJar());

        ZstdDecompressorFactory factory = new ZstdJNIDecompressorFactoryProvider().get(ZstdDecompressor.DEFAULT_BUFFER_SIZE);
        ZstdDecompressor decompressor = factory.create();
        for (TestChunks.Chunk chunk : chunks) {
            final byte[] actual = decompressor.decompress(chunk.getCompressed());
            final byte[] expected = chunk.getDecompressed();
            assertArrayEquals(expected, actual);
        }
    }
}
