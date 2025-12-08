package dev.freya02.discord.zstd.jni;

import dev.freya02.discord.zstd.TestChunks;
import dev.freya02.discord.zstd.api.DiscordZstdContext;
import dev.freya02.discord.zstd.api.DiscordZstdDecompressor;
import dev.freya02.discord.zstd.api.DiscordZstdDecompressorFactory;
import dev.freya02.discord.zstd.api.DiscordZstdProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class ZstdJNITest {

    private static List<List<TestChunks.Chunk>> shards;

    @BeforeAll
    public static void setup() {
        shards = TestChunks.get();
    }

    @Test
    public void test_decompression() {
        DiscordZstdDecompressorFactory factory = DiscordZstdProvider.get().createDecompressorFactory(DiscordZstdDecompressor.DEFAULT_BUFFER_SIZE);
        for (List<TestChunks.Chunk> shard : shards) {
            DiscordZstdDecompressor decompressor = factory.create();
            for (TestChunks.Chunk chunk : shard) {
                final byte[] actual = decompressor.decompress(chunk.zstdCompressed());
                final byte[] expected = chunk.decompressed();
                assertArrayEquals(expected, actual);
            }
            decompressor.close();
        }
    }

    @Test
    public void test_input_stream() throws IOException {
        for (List<TestChunks.Chunk> shard : shards) {
            DiscordZstdContext context = DiscordZstdProvider.get().createContext();
            for (TestChunks.Chunk chunk : shard) {
                try (InputStream stream = context.createInputStream(chunk.zstdCompressed())) {
                    final byte[] actual = stream.readAllBytes();
                    final byte[] expected = chunk.decompressed();
                    assertArrayEquals(expected, actual);
                }
            }
            context.close();
        }
    }
}
