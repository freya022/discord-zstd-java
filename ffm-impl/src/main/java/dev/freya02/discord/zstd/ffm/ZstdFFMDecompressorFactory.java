package dev.freya02.discord.zstd.ffm;

import dev.freya02.discord.zstd.api.ZstdDecompressor;
import dev.freya02.discord.zstd.api.ZstdDecompressorFactory;
import dev.freya02.discord.zstd.api.ZstdNativesLoader;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ZstdFFMDecompressorFactory implements ZstdDecompressorFactory {

    @Override
    public ZstdDecompressor get(int bufferSize) {
        if (!ZstdNativesLoader.isLoaded()) {
            throw new IllegalStateException("Natives are not loaded yet, see ZstdNativesLoader");
        }
        return new ZstdFFMDecompressor(bufferSize);
    }
}
