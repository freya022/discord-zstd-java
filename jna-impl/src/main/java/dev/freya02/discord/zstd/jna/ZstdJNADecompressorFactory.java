package dev.freya02.discord.zstd.jna;

import dev.freya02.discord.zstd.api.DiscordZstdDecompressor;
import dev.freya02.discord.zstd.api.DiscordZstdDecompressorFactory;
import dev.freya02.discord.zstd.api.DiscordZstdNativesLoader;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ZstdJNADecompressorFactory implements DiscordZstdDecompressorFactory {

    private final int bufferSize;

    protected ZstdJNADecompressorFactory(int bufferSize) {
        if (bufferSize < DiscordZstdDecompressor.MIN_BUFFER_SIZE && bufferSize != DiscordZstdDecompressor.ZSTD_RECOMMENDED_BUFFER_SIZE)
            throw new IllegalArgumentException("Buffer must be larger than or equal to " + DiscordZstdDecompressor.MIN_BUFFER_SIZE + ", provided " + bufferSize);
        this.bufferSize = bufferSize;
    }

    @Override
    public DiscordZstdDecompressor create() {
        if (!DiscordZstdNativesLoader.isLoaded()) {
            throw new IllegalStateException("Natives are not loaded yet, see ZstdNativesLoader");
        }
        return new ZstdJNADecompressor(bufferSize);
    }
}
