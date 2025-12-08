package dev.freya02.discord.zstd.jna;

import dev.freya02.discord.zstd.TestChunks;
import dev.freya02.discord.zstd.api.DiscordZstd;
import dev.freya02.discord.zstd.api.DiscordZstdDecompressor;
import dev.freya02.discord.zstd.api.DiscordZstdDecompressorFactory;
import dev.freya02.discord.zstd.api.DiscordZstdNativesLoader;
import dev.freya02.discord.zstd.internal.NativeUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class ZstdJNATest {

    private static List<List<TestChunks.Chunk>> shards;

    @BeforeAll
    public static void setup() {
        shards = TestChunks.get();
    }

    @Test
    public void test_decompression() throws IOException {
        NativeUtil.System system = NativeUtil.getSystem();
        String absoluteNativeResource = String.format("/dev/freya02/discord/zstd/natives/%s/libzstd.%s", system.platform, system.sharedLibraryExtension);
        DiscordZstdNativesLoader.loadFromJar(absoluteNativeResource, DiscordZstd.class);

        DiscordZstdDecompressorFactory factory = new ZstdJNADecompressorFactoryProvider().get(DiscordZstdDecompressor.DEFAULT_BUFFER_SIZE);
        for (List<TestChunks.Chunk> shard : shards) {
            DiscordZstdDecompressor decompressor = factory.create();
            for (TestChunks.Chunk chunk : shard) {
                final byte[] actual = decompressor.decompress(chunk.zstdCompressed());
                final byte[] expected = chunk.decompressed();
                assertArrayEquals(expected, actual);
            }
        }
    }
}
