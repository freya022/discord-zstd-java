package dev.freya02.discord.zstd.jna;

import dev.freya02.discord.zstd.api.ZstdDecompressorFactory;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ZstdJNADecompressorFactoryProvider {

    public ZstdDecompressorFactory get(int bufferSizeHint) {
        return new ZstdJNADecompressorFactory(bufferSizeHint);
    }
}
