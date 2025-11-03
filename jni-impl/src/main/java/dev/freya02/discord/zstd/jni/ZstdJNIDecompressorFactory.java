package dev.freya02.discord.zstd.jni;

import dev.freya02.discord.zstd.api.ZstdDecompressor;
import dev.freya02.discord.zstd.api.ZstdDecompressorFactory;
import dev.freya02.discord.zstd.api.ZstdNativesLoader;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ZstdJNIDecompressorFactory implements ZstdDecompressorFactory {

    @Override
    public ZstdDecompressor get(int bufferSize) {
        if (!ZstdNativesLoader.isLoaded()) {
            throw new IllegalStateException("Natives are not loaded yet, see ZstdNativesLoader");
        }
        return new ZstdJNIDecompressor(bufferSize);
    }
}
