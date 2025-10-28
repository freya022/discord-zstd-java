package dev.freya02.discord.zstd.jna;

import dev.freya02.discord.zstd.ZstdDecompressor;
import dev.freya02.discord.zstd.ZstdDecompressorFactory;
import dev.freya02.discord.zstd.ZstdNativesLoader;
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
