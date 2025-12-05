package dev.freya02.discord.zstd.jni;

import dev.freya02.discord.zstd.api.DiscordZstdDecompressor;
import dev.freya02.discord.zstd.api.DiscordZstdDecompressorFactory;
import org.jspecify.annotations.NullMarked;

@NullMarked
class DiscordZstdJNIDecompressorFactory implements DiscordZstdDecompressorFactory {

    private final int bufferSizeHint;

    protected DiscordZstdJNIDecompressorFactory(int bufferSizeHint) {
        this.bufferSizeHint = bufferSizeHint;
    }

    @Override
    public DiscordZstdDecompressor create() {
        return new DiscordZstdJNIDecompressor(bufferSizeHint);
    }
}
