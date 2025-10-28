package dev.freya02.discord.zstd.jna;

import dev.freya02.discord.zstd.api.ZstdDecompressor;
import dev.freya02.discord.zstd.api.ZstdDecompressorFactory;
import dev.freya02.discord.zstd.api.ZstdNativesLoader;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ZstdJNADecompressorFactory implements ZstdDecompressorFactory {

    @Override
    public ZstdDecompressor get(int maxBufferSize) {
        if (!ZstdNativesLoader.isLoaded()) {
            throw new IllegalStateException("Natives are not loaded yet, see ZstdNativesLoader");
        }
        return new ZstdJNADecompressor(maxBufferSize);
    }
}
