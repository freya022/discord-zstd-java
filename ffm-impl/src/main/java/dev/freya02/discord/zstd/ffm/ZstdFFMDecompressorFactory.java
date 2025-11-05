package dev.freya02.discord.zstd.ffm;

import dev.freya02.discord.zstd.api.ZstdDecompressor;
import dev.freya02.discord.zstd.api.ZstdDecompressorFactory;
import dev.freya02.discord.zstd.api.ZstdNativesLoader;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ZstdFFMDecompressorFactory implements ZstdDecompressorFactory {

    private final int bufferSize;

    protected ZstdFFMDecompressorFactory(int bufferSize) {
        if (bufferSize < ZstdDecompressor.MIN_BUFFER_SIZE && bufferSize != ZstdDecompressor.RECOMMENDED_BUFFER_SIZE)
            throw new IllegalArgumentException("Buffer must be larger than or equal to " + ZstdDecompressor.MIN_BUFFER_SIZE + ", provided " + bufferSize);
        this.bufferSize = bufferSize;
    }

    @Override
    public ZstdDecompressor create() {
        if (!ZstdNativesLoader.isLoaded()) {
            throw new IllegalStateException("Natives are not loaded yet, see ZstdNativesLoader");
        }
        return new ZstdFFMDecompressor(bufferSize);
    }
}
