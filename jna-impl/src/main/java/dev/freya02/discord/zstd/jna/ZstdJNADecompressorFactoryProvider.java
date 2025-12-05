package dev.freya02.discord.zstd.jna;

import dev.freya02.discord.zstd.api.DiscordZstdDecompressorFactory;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ZstdJNADecompressorFactoryProvider {

    public DiscordZstdDecompressorFactory get(int bufferSizeHint) {
        return new ZstdJNADecompressorFactory(bufferSizeHint);
    }
}
