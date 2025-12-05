package dev.freya02.discord.zstd.ffm;

import dev.freya02.discord.zstd.api.ZstdDecompressor;
import dev.freya02.discord.zstd.api.ZstdDecompressorFactory;
import org.jspecify.annotations.NullMarked;

@NullMarked
class ZstdFFMDecompressorFactory implements ZstdDecompressorFactory {

    private final int bufferSizeHint;

    protected ZstdFFMDecompressorFactory(int bufferSizeHint) {
        this.bufferSizeHint = bufferSizeHint;
    }

    @Override
    public ZstdDecompressor create() {
        return new ZstdFFMDecompressor(bufferSizeHint);
    }
}
