package dev.freya02.discord.zstd.ffm;

import dev.freya02.discord.zstd.ZstdDecompressor;
import dev.freya02.discord.zstd.ZstdDecompressorFactory;
import dev.freya02.discord.zstd.ZstdNativesLoader;

import javax.annotation.Nonnull;

public class ZstdFFMDecompressorFactory implements ZstdDecompressorFactory {
    @Nonnull
    @Override
    public ZstdDecompressor get(int maxBufferSize) {
        if (!ZstdNativesLoader.isLoaded()) {
            throw new IllegalStateException("Natives are not loaded yet, see ZstdNativesLoader");
        }
        return new ZstdFFMDecompressor(maxBufferSize);
    }
}
