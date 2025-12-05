package dev.freya02.discord.zstd.ffm;

import dev.freya02.discord.zstd.api.ZstdDecompressor;
import dev.freya02.discord.zstd.api.ZstdDecompressorFactory;
import org.jspecify.annotations.NullMarked;

@NullMarked
class ZstdFFMDecompressorFactory implements ZstdDecompressorFactory {

    private final int bufferSize;

    protected ZstdFFMDecompressorFactory(int bufferSize) {
        if (bufferSize < ZstdDecompressor.MIN_BUFFER_SIZE && bufferSize != ZstdDecompressor.ZSTD_RECOMMENDED_BUFFER_SIZE)
            throw new IllegalArgumentException("Buffer must be larger than or equal to " + ZstdDecompressor.MIN_BUFFER_SIZE + ", provided " + bufferSize);
        this.bufferSize = bufferSize;
    }

    @Override
    public ZstdDecompressor create() {
        return new ZstdFFMDecompressor(bufferSize);
    }
}
