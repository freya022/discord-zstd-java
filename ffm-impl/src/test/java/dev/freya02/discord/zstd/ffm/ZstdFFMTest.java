package dev.freya02.discord.zstd.ffm;

import dev.freya02.discord.zstd.TestChunks;
import dev.freya02.discord.zstd.api.ZstdDecompressor;
import dev.freya02.discord.zstd.api.ZstdNativesLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ZstdFFMTest {

    private static List<TestChunks.Chunk> chunks;

    @BeforeAll
    public static void setup() {
        chunks = TestChunks.get(TestChunks.Compression.ZSTD);
    }

    @Test
    public void test_decompression() throws IOException {
        assertTrue(ZstdNativesLoader.loadFromJar());

        ZstdFFMDecompressorFactory factory = new ZstdFFMDecompressorFactory();
        ZstdDecompressor decompressor = factory.get(2048);
        for (TestChunks.Chunk chunk : chunks) {
            final byte[] actual = decompressor.decompress(chunk.getCompressed());
            final byte[] expected = chunk.getDecompressed();
            assertArrayEquals(expected, actual);
        }
    }
}
