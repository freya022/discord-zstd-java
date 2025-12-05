package dev.freya02.discord.zstd.jni;

import dev.freya02.discord.zstd.api.ZstdDecompressor;
import dev.freya02.discord.zstd.api.ZstdDecompressorFactory;
import org.jspecify.annotations.NullMarked;

@NullMarked
class ZstdJNIDecompressorFactory implements ZstdDecompressorFactory {

    private final int bufferSizeHint;

    protected ZstdJNIDecompressorFactory(int bufferSizeHint) {
        this.bufferSizeHint = bufferSizeHint;
    }

    @Override
    public ZstdDecompressor create() {
        return new ZstdJNIDecompressor(bufferSizeHint);
    }
}
