package dev.freya02.discord.zstd.ffm;

import dev.freya02.discord.zstd.api.*;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@NullMarked
public class DiscordZstdFFM implements DiscordZstd {
    private static final Logger LOGGER = LoggerFactory.getLogger(DiscordZstdFFM.class);

    public DiscordZstdFFM() throws IOException {
        LOGGER.debug("Using FFM implementation of discord-zstd-java");

        // Load natives if they weren't already
        DiscordZstdNativesLoader.loadFromJar();
    }

    @Override
    public DiscordZstdContext createContext() {
        return new DiscordZstdFFMContext();
    }

    @Override
    public DiscordZstdDecompressorFactory createDecompressorFactory(int bufferSizeHint) {
        if (bufferSizeHint < DiscordZstdDecompressor.MIN_BUFFER_SIZE && bufferSizeHint != DiscordZstdDecompressor.ZSTD_RECOMMENDED_BUFFER_SIZE)
            throw new IllegalArgumentException("Buffer must be larger than or equal to " + DiscordZstdDecompressor.MIN_BUFFER_SIZE + ", provided " + bufferSizeHint);
        return new DiscordZstdFFMDecompressorFactory(bufferSizeHint);
    }
}
