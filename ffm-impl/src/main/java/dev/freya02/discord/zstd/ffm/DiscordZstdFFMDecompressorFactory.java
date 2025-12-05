package dev.freya02.discord.zstd.ffm;

import dev.freya02.discord.zstd.api.DiscordZstdDecompressor;
import dev.freya02.discord.zstd.api.DiscordZstdDecompressorFactory;
import org.jspecify.annotations.NullMarked;

@NullMarked
class DiscordZstdFFMDecompressorFactory implements DiscordZstdDecompressorFactory {

    private final int bufferSizeHint;

    protected DiscordZstdFFMDecompressorFactory(int bufferSizeHint) {
        this.bufferSizeHint = bufferSizeHint;
    }

    @Override
    public DiscordZstdDecompressor create() {
        return new DiscordZstdFFMDecompressor(bufferSizeHint);
    }
}
