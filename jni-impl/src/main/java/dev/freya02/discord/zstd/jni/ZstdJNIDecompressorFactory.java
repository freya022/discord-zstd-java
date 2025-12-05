package dev.freya02.discord.zstd.jni;

import dev.freya02.discord.zstd.api.ZstdDecompressor;
import dev.freya02.discord.zstd.api.ZstdDecompressorFactory;
import org.jspecify.annotations.NullMarked;

@NullMarked
class ZstdJNIDecompressorFactory implements ZstdDecompressorFactory {

    private final int bufferSize;

    protected ZstdJNIDecompressorFactory(int bufferSize) {
        if (bufferSize < ZstdDecompressor.MIN_BUFFER_SIZE && bufferSize != ZstdDecompressor.ZSTD_RECOMMENDED_BUFFER_SIZE)
            throw new IllegalArgumentException("Buffer must be larger than or equal to " + ZstdDecompressor.MIN_BUFFER_SIZE + ", provided " + bufferSize);
        this.bufferSize = bufferSize;
    }

    @Override
    public ZstdDecompressor create() {
        return new ZstdJNIDecompressor(bufferSize);
    }
}
