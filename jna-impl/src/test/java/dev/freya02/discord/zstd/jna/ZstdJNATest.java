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

    private static List<TestChunks.Chunk> chunks;

    @BeforeAll
    public static void setup() {
        chunks = TestChunks.get(TestChunks.Compression.ZSTD);
    }

    @Test
    public void test_decompression() throws IOException {
        NativeUtil.System system = NativeUtil.getSystem();
        String absoluteNativeResource = String.format("/dev/freya02/discord/zstd/natives/%s/libzstd.%s", system.platform, system.sharedLibraryExtension);
        DiscordZstdNativesLoader.loadFromJar(absoluteNativeResource, DiscordZstd.class);

        DiscordZstdDecompressorFactory factory = new ZstdJNADecompressorFactoryProvider().get(DiscordZstdDecompressor.DEFAULT_BUFFER_SIZE);
        DiscordZstdDecompressor decompressor = factory.create();
        for (TestChunks.Chunk chunk : chunks) {
            final byte[] actual = decompressor.decompress(chunk.getCompressed());
            final byte[] expected = chunk.getDecompressed();
            assertArrayEquals(expected, actual);
        }
    }
}
