package dev.freya02.discord.zstd.jni;

import dev.freya02.discord.zstd.api.ZstdDecompressorFactory;
import dev.freya02.discord.zstd.api.ZstdDecompressorFactoryProvider;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ZstdJNIDecompressorFactoryProvider implements ZstdDecompressorFactoryProvider {

    @Override
    public ZstdDecompressorFactory get(int bufferSizeHint) {
        return new ZstdJNIDecompressorFactory(bufferSizeHint);
    }
}
