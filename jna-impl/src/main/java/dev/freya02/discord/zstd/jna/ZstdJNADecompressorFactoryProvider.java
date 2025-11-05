package dev.freya02.discord.zstd.jna;

import dev.freya02.discord.zstd.api.ZstdDecompressorFactory;
import dev.freya02.discord.zstd.api.ZstdDecompressorFactoryProvider;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ZstdJNADecompressorFactoryProvider implements ZstdDecompressorFactoryProvider {

    @Override
    public ZstdDecompressorFactory get(int bufferSizeHint) {
        return new ZstdJNADecompressorFactory(bufferSizeHint);
    }
}
